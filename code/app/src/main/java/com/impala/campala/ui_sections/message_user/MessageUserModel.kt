package com.impala.campala.ui_sections.message_user

import com.impala.campala.ObservableModel
import com.impala.campala.model.MessageModel
import com.impala.campala.model.MessagesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class MessageUserModel(
    val scope: CoroutineScope,
    ) : ObservableModel<MessageUserModel>() {
//    val ownerMessages = mutableListOf<MessageModel>()
//    val entrantMessages = mutableListOf<MessageModel>()

    private var reservationId : String = ""
    private var entrantId : String = ""
    private var ownerId : String = ""
    private var messages = mutableListOf<MessageModel>()

    /**
     * Loads the message thread for a reservation, made so that we don't have to provide this at constructor time
     * @param reservationId the ID of the reservation
     */
    public fun loadThread(reservationId: String) {
        this.reservationId = reservationId

        messages = mutableListOf()
        entrantId = ""
        ownerId = ""
        notifyObservers(this)

        refresh()
    }

    /**
     * Refreshes the message log. returns the MessageUserModel, which may be helpful.
     *
     * Must be done at least once when MessageUserModel is opened.
     */
    public fun refresh(): MessageUserModel {
        scope.launch {
            val repository = MessagesRepository.getOrCreateThreadByReservation(reservationId)

            //TODO: Optimize this so it adds new messages instead of clearing ALL OLD messages then adding ALL messages.
            messages = repository.messageThread

//            ownerMessages.clear()
//            ownerMessages.addAll(messages.filter { it.fromOwner })
//
//            entrantMessages.clear()
//            entrantMessages.addAll(messages.filter { !it.fromOwner })

            entrantId = repository.entrantId
            ownerId = repository.ownerId

            notifyObservers(this@MessageUserModel)
        }
        return this
    }

    /**
     * Sends a given message to the list of messages.
     */
    public fun send(text: String, owner: Boolean) {
        scope.launch {
            MessagesRepository.sendMessage(text, owner, MessagesRepository.getOrCreateThreadByReservation(reservationId))
            refresh()
        }
    }

    fun threadInfo(): Map<String, String> {
        return mapOf(
            "entrantId" to entrantId,
            "ownerId" to ownerId,
            "reservationId" to reservationId
        )
    }

    fun messages() : MutableList<MessageModel> {
        return messages
    }
}