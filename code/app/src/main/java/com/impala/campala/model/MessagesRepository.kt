package com.impala.campala.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class MessagesRepository {
    private val collection by lazy { Firebase.firestore.collection("messages") }

    /**
     * Gets a message thread by its reservation, if it doesn't exist it creates one
     *
     * @param reservationId ID for reservation
     *
     * @return the created/existing message thread
     */
    suspend fun getOrCreateThreadByReservation(reservationId: String): MessagesModel{
        try {
            return collection.whereEqualTo("reservationID", reservationId).get().await()
                .map { MessagesModel.load(it) }[0]
        } catch (e: IndexOutOfBoundsException) {
            val repo = ReservationRepository()
            return MessagesModel(
                ownerId = repo.getReservationById(reservationId)!!.ownerId,
                entrantId = repo.getReservationById(reservationId)!!.entrantId,
                reservationId = reservationId
            )
        } catch (e: Exception) {
            throw e
        }
    }


    /**
     * Sends an inputted message by adding to the end of the message thread and returning updated
     *
     * @param message Text that user/owner is sending
     * @param isFromOwner Flag to tell who is sending
     * @param model MessagesModel to use for chat
     */
    fun sendMessage(message: String,isFromOwner: Boolean,model: MessagesModel): Unit{
        model.messageThread.addLast(MessageModel(message,isFromOwner))
        model.save()
    }
}