package com.impala.campala.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Repository for storing Messages between Owners and Entrants who reserved campsites
 *
 * Follows singleton pattern
 */
object MessagesRepository {
    private val collection by lazy { Firebase.firestore.collection("messages") }

    /**
     * Gets a message thread by its reservation, if it doesn't exist it creates one
     *
     * @param reservationId ID for reservation
     *
     * @return the created/existing message thread
     */
    suspend fun getOrCreateThreadByReservation(reservationId: String): MessagesModel{
        val messages = getThreadByReservation(reservationId)
        if(messages != null)
            return messages
        return MessagesModel(
            ownerId = ReservationRepository.getReservationById(reservationId)!!.ownerId,
            entrantId = ReservationRepository.getReservationById(reservationId)!!.entrantId,
            reservationId = reservationId
        )
    }

    private suspend fun getThreadByReservation(reservationId: String): MessagesModel?{
        return try {
            val snapshot = collection.document(reservationId).get().await()
            snapshot.toObject(MessagesModel::class.java) // null if the document doesn't exist
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
    }


    /**
     * Sends an inputted message by adding to the end of the message thread and returning updated
     *
     * @param message Text that user/owner is sending
     * @param isFromOwner Flag to tell who is sending
     * @param model MessagesModel to use for chat
     */
    suspend fun sendMessage(message: String,isFromOwner: Boolean,model: MessagesModel): Unit{
        model.messageThread.addLast(MessageModel(message,isFromOwner, Clock.System.now()))
        model.save().await()
    }

    /**
     * Gets all messages for an Owner
     *
     * @param ownerId Owner's ID
     */
    suspend fun getThreadByOwner(ownerId: String): List<MessagesModel> {
        val threads = collection.whereEqualTo("ownerId", ownerId).get().await()
        return threads.toObjects(MessagesModel::class.java)
    }

    /**
     * Gets all messages for an entrant
     *
     * @param entrantId Entrant's ID
     */
    suspend fun getThreadByEntrant(entrantId: String): List<MessagesModel> {
        val threads = collection.whereEqualTo("entrantId", entrantId).get().await()
        return threads.toObjects(MessagesModel::class.java)
    }

    /**
     * Gets all threads for admins
     */
    suspend fun getAllThreads(): List<MessagesModel> {
        val threads = collection.get().await()
        return threads.toObjects(MessagesModel::class.java)
    }

    /**
     * Deletes Thread from database
     *
     * @param reservationId messageModel to delete
     */
    suspend fun deleteThread(reservationId: String): Unit {
        collection.document(reservationId).delete().await()
    }
}