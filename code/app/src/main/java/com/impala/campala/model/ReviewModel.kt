package com.impala.campala.model

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A review that an entrant leaves on a campsite after a past stay
 *
 * @param posterId The UID of the entrant who wrote the review
 * @param campsiteId The ID of the campsite being reviewed
 * @param rating The rating out of 5, can be a half (eg: 3.5)
 * @param reviewText The written part of the review, can be empty if the entrant only left a rating
 * @param id The ID that the object has in the database
 *
 * @property doc The reference to the Firebase document containing this object
 */
data class ReviewModel @OptIn(ExperimentalUuidApi::class) constructor(
    val posterId: String = "",
    val campsiteId: String = "",
    val rating: Double = 0.0,
    val reviewText: String = "",
    val id: String = Uuid.random().toString()
) {
    companion object {
        fun load(doc: DocumentSnapshot): ReviewModel {
            return doc.toObject(ReviewModel::class.java)!!
        }
    }

    @get:Exclude
    var doc: DocumentReference? = null
    /**
     * Saves the review object in the Firebase reviews collection
     *
     * @return A reference to the Firebase document of this review model
     */
    fun save(): Task<Void> {
        doc = Firebase.firestore.document("reviews/" + id)
        return doc!!.set(this)
    }
}
