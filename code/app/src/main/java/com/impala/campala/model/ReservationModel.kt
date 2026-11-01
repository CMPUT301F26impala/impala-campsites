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

data class ReservationModel @OptIn(ExperimentalUuidApi::class) constructor(
    val campsiteId: String="", // Campsite
    val ownerId: String="", // Owner of campsite
    val entrantId: String="", // Entrant that reserved
    var startDate: com.google.firebase.Timestamp= Timestamp.now(), // start of stay
    var endDate: com.google.firebase.Timestamp= Timestamp.now(), // end of stay
    var rating: Int? = null, // rating from 1 to 5, null if hasRating == false
    val id: String = Uuid.random().toString()
) {
    @get:Exclude
    val isPast: Boolean
        // TODO: use server time instead of this, since clients can change
        //  their system time. from brief research this might need server functions
        get() = Timestamp.now() > endDate
    @get:Exclude
    val hasRating: Boolean
        get() = rating != null

    companion object {
        fun load(doc: DocumentSnapshot): ReservationModel {
            return doc.toObject(ReservationModel::class.java)!!
        }
    }

    @get:Exclude
    var doc: DocumentReference? = null
    fun save(): Task<Void> {
        doc = Firebase.firestore.document("reservations/" + id)
        return doc!!.set(this)
    }


}