package com.impala.campala.model

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore

/**
 * An entrant profile, it shares the same userId as the User class connected to the device
 * But it is separate so that it can be disabled without banning the USER itself, just by removing the Entrant
 *
 * @param userId The UID of the User this profile belongs to
 * @param name Entrant's name
 * @param email Entrant's email
 * @param phoneNumber Entrant's phone number
 * @param imageId ID of the entrant's profile picture in the images collection, empty if they have no picture
 *
 * @property doc The reference to the Firebase document containing this object
 */
data class EntrantProfileModel(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var imageId: String = "",
    var notificationId: String=""
) {
    companion object {
        fun load(doc: DocumentSnapshot): EntrantProfileModel {
            return doc.toObject(EntrantProfileModel::class.java)!!
        }
    }

    @get:Exclude
    var doc: DocumentReference? = null
    /**
     * Saves the entrant profile object in the Firebase entrants collection
     *
     * @return A reference to the Firebase document of this entrant profile model
     */
    fun save(): Task<Void> {
        require(userId.isNotBlank()) { "EntrantProfileModel userId cannot be blank" }
        doc = Firebase.firestore.document("entrants/" + userId)
        return doc!!.set(this)
    }
}
