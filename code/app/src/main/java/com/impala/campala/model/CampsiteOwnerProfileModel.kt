package com.impala.campala.model

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore

/**
 * This shares the same User ID as the associated User and EntrantProfile objects, but this tracks specifically the campsite owner details
 * This enables us to disable/ban a campsite owner profile without banning the user as a whole.
 *
 * @param userId The UID of the User this profile belongs to
 * @param ownedCampsites IDs of the campsites this owner owns
 * @param ownedLotteries IDs of the lotteries this owner owns
 */
data class CampsiteOwnerProfileModel(
    var userId: String = "",
    var ownedCampsites: MutableList<String> = mutableListOf(),
    var ownedLotteries: MutableList<String> = mutableListOf()
) {
    companion object {
        fun load(doc: DocumentSnapshot): CampsiteOwnerProfileModel {
            return doc.toObject(CampsiteOwnerProfileModel::class.java)!!
        }
    }

    /**
     * Saves the campsite owner object in the Firebase campsiteOwners collection
     *
     * @return A reference to the Firebase document of this campsite owner model
     */
    @get:Exclude
    var doc: DocumentReference? = null
    fun save(): Task<Void> {
        doc = Firebase.firestore.document("campsiteOwners/" + userId)
        return doc!!.set(this)
    }
}
