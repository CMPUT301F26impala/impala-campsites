package com.impala.campala.ui_sections.admin_messages

import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.MessageModel
import com.impala.campala.model.MessagesModel
import com.impala.campala.model.MessagesRepository
import com.impala.campala.model.ReservationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A helper class that represents a single internal MessageModel and it's thread
 *
 * @param thread the thread the message belongs to, needed to delete the message
 * @param message the message itself
 */
data class AdminMessage(
    val thread: MessagesModel,
    val message: MessageModel
)

/**
 * Model for the admin's messages tab, for viewing every message and removing them
 *
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property messages Every message from every thread, newest first
 * @property campsiteNames A helper to get campsite names from a thread's reservation ID
 */
class AdminMessagesModel(
    val scope: CoroutineScope
) : ObservableModel<AdminMessagesModel>() {
    var messages: List<AdminMessage> = emptyList()
    var campsiteNames: Map<String, String> = emptyMap()

    /**
     * Loads every thread, turns it into one list of messages sorted by date (unwraps the threads), and looks up each thread's campsite name
     */
    fun initialize() {
        scope.launch {
            val threads = MessagesRepository.getAllThreads()
            messages = threads.flatMap { thread ->
                thread.messageThread.map { message ->
                    AdminMessage(thread, message)
                }
            }.sortedByDescending { it.message.date }

            // Threads only store the reservation, so we need that AND campsite to get the name
            val names = mutableMapOf<String, String>()
            for (thread in threads) {
                val campsiteId = ReservationRepository.getReservationById(thread.reservationId)?.campsiteId ?: continue
                val campsite = CampsiteRepository.getCampsiteById(campsiteId) ?: continue
                names[thread.reservationId] = campsite.name
            }
            campsiteNames = names
            notifyObservers(this@AdminMessagesModel)
        }
    }

    /**
     * Deletes a message from its thread, then reloads the list
     *
     * @param adminMessage the AdminMessage wrapper object to be deleted
     */
    fun deleteMessage(adminMessage: AdminMessage) {
        scope.launch {
            MessagesRepository.deleteMessage(adminMessage.thread, adminMessage.message)
            initialize()
        }
    }
}
