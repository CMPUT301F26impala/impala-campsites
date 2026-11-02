package com.impala.campala.model

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await


/**
 * Repository for storing the app's reservations.
 *
 * Model for anything in the app that wants to access any
 * reservation info, with filtering by owner or entrant
 *
 * @property collection Reference to the Firestore reservations collection
 */
class ReservationRepository {
    private val collection by lazy { Firebase.firestore.collection("reservations") }

    /**
     * Gets a single reservation by its ID
     *
     * @param reservationId ID for reservation
     *
     * @return the matching reservation or null if none match
     */
    suspend fun getReservationById(reservationId: String): ReservationModel? {
        try {
            return ReservationModel.load(collection.document(reservationId).get().await())
        } catch (e: NullPointerException) {
            // realistically the model should probably return a nullable type as well
            // but since i haphazardly put a null assertion in the campsite model and
            // everyone is following my footstep, ill leave it for consistency for now
            return null
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Gets all Reservations (past and future) for campsites owned by a specified owner
     *
     * Used for the owner's reservations screen
     *
     * @param ownerId ID for owner
     *
     * @return list of owner's reservations
     */
    suspend fun getReservationsByOwner(ownerId: String): List<ReservationModel> {
        return collection.whereEqualTo("ownerId", ownerId).get().await()
            .map { ReservationModel.load(it) }
    }

    /**
     * Gets all Reservations from a user
     *
     * Used for the entrant reservations screen
     *
     * @param UserId ID for user
     *
     * @return list of entrant's reservations
     */
    suspend fun getReservationsByUser(UserId: String): List<ReservationModel> {
        return collection.whereEqualTo("entrantId", UserId).get().await()
            .map { ReservationModel.load(it) }
    }

    suspend fun getReservationsByDateRange(start: Timestamp, end: Timestamp): List<ReservationModel> {
        // someone should use their 272 knowledge and clean this up
        return collection.where(
            Filter.or(
                // start<=startDate<=end
                Filter.and(
                    Filter.greaterThanOrEqualTo("startDate", start),
                    Filter.lessThanOrEqualTo("startDate", end)
                ),
                // start<=endDate<=end
                Filter.and(
                    Filter.greaterThanOrEqualTo("endDate", start),
                    Filter.lessThanOrEqualTo("endDate", end)
                ),
                // startDate<=start<=endDate
                Filter.and(
                    Filter.lessThanOrEqualTo("startDate", start),
                    Filter.greaterThanOrEqualTo("endDate", start)
                ),
                // startDate<=end<=endDate
                Filter.and(
                    Filter.lessThanOrEqualTo("startDate", end),
                    Filter.greaterThanOrEqualTo("endDate", end)
                ),
            )
        ).get().await().map { ReservationModel.load(it) }
    }

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
        collection.document(reservation.id).delete().await()
    }
}