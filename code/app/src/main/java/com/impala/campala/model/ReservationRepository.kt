package com.impala.campala.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await


/**
 * Repository for storing the app's reservations.
 *
 * Model for anything in the app that wants to access any
 * reservation info, with filtering by owner or entrant
 *
 * @property db Firestore database lazy reference
 */
class ReservationRepository {
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    /**
     * Gets a single reservation by its ID
     *
     * @param reservationId ID for reservation
     *
     * @return the matching reservation or null if none match
     */
    suspend fun getReservationById(reservationId:String): ReservationModel?{}

    /**
     * Gets all Reservations (past and future) for campsites owned by a specified owner
     *
     * Used for the owner's reservations screen
     *
     * @param ownerId ID for owner
     *
     * @return list of owner's reservations
     */
    suspend fun getReservationsByOwner(ownerId: String): List<ReservationModel>{}

    /**
     * Gets all Reservations from a user
     *
     * Used for the entrant reservations screen
     *
     * @param UserId ID for user
     *
     * @return list of entrant's reservations
     */
    suspend fun getReservationsByUser(UserId:String): List<ReservationModel>{}

    /**
     * Saves a Reservation to the database
     *
     *  When a screen updates a reservation, it's controller will end up calling this method
     *      with the new modifier reservation object, then go and update the one with a matching
     *      ID. If the reservation didn't exist before it will create it
     *
     *  Suspends until the write finishes, and throws if it fails
     *
     * @param reservation ReservationModel to be saved
     */
    suspend fun saveReservation(reservation: ReservationModel): Unit {
        reservation.save().await()
    }

    /**
     * Deletes Reservation from database
     *
     * Suspends until the delete finishes, and throws if it fails
     *
     * @param reservation ReservationModel to delete
     */
    suspend fun deleteReservation(reservation: ReservationModel): Unit {
        db.collection("reservations").document(reservation.id).delete().await()
    }
}