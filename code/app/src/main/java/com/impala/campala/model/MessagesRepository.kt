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
    suspend fun getThreadByReservation(reservationId: String): MessagesModel{
        try {
            return MessagesModel.load(collection.document(reservationId).get().await())
        } catch (e: NullPointerException) {
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

    
}