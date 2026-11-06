package com.impala.campala.model

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * This shares the same User ID as the associated User and EntrantProfile objects, but this tracks specifically the campsite owner details
 * This enables us to disable/ban a campsite owner profile without banning the user as a whole.
 *
 * @param campsiteId The ID of the campsite being reserved
 * @param ownerId The ID of the owner of the campsite being reserved
 * @param entrantId The ID of the entrant making the reservation
 * @param startDate The first date of a reservation
 * @param endDate The last date of the reservation
 * @param hasRating True if there is a rating attached to this reservation
 * @param lotteryId The ID of the lottery that this reservation was created from
 * @param id The ID that the object has in the database
 *
 * @property doc The reference to the Firebase document containing this object
 * @property isPast True if the reservation is over
 */
data class ReservationModel @OptIn(ExperimentalUuidApi::class) constructor(
    val campsiteId: String = "", // Campsite
    val ownerId: String = "", // Owner of campsite
    val entrantId: String = "", // Entrant that reserved
    var startDate: com.google.firebase.Timestamp = Timestamp.now(), // start of stay
    var endDate: com.google.firebase.Timestamp = Timestamp.now(), // end of stay
    var hasRating: Boolean = false, // true once the entrant has rated this stay, so it can't be rated again (the rating itself is only kept on the campsite)
    val lotteryId: String? = null, // The id of the lottery that the user got this reservation from, used so that the UI knows which lottery wins were already acted on
    var hasBeenCancelled: Boolean = false, // true once the reservation has been cancelled or rejected
    val id: String = Uuid.random().toString()
) {
    @get:Exclude
    val isPast: Boolean
        // TODO: use server time instead of this, since clients can change
        //  their system time. from brief research this might need server functions
        get() = Timestamp.now() > endDate

    companion object {
        fun load(doc: DocumentSnapshot): ReservationModel {
            return doc.toObject(ReservationModel::class.java)!!
        }
    }

    @get:Exclude
    var doc: DocumentReference? = null
    /**
     * Saves the reservation object in the Firebase reservations collection
     *
     * @return A reference to the Firebase document of this reservation model
     */
    fun save(): Task<Void> {
        doc = Firebase.firestore.document("reservations/" + id)
        return doc!!.set(this)
    }
}