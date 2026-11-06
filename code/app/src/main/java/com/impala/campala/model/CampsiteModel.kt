package com.impala.campala.model

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class CampsiteModel @OptIn(ExperimentalUuidApi::class) constructor(
    var name: String = "",
    var location: String = "",
    var description: String = "",
    var maxOccupancy: Int = 0,
    var mapLocation: String = "", // probably pair of float for lat and longitude
    var imageUrl: String = "",
    var tags: MutableList<String> = mutableListOf(),
    var rules: String="",
    // ratingTotal is the sum of all star ratings a campsite has gotten (eg: 3.5 + 5 + 4.5)
    // ratingCount is the number of ratings, the average is just total / count (use the getter below)
    var ratingTotal: Double = 0.0,
    var ratingCount: Int = 0,
    var ownerId: String = "", // UID of the campsite owner who created it, the Firestore security rules use this to decide who can edit/delete it
    var id: String = Uuid.random().toString()
) {
    /**
     * The campsite's average rating out of 5, or null if nobody has rated it yet
     */
    @get:Exclude
    val averageRating: Double?
        get() = if (ratingCount == 0) null else ratingTotal / ratingCount

    companion object {
        fun load(doc: DocumentSnapshot): CampsiteModel {
            return doc.toObject(CampsiteModel::class.java)!!
        }
    }

    @get:Exclude
    var doc: DocumentReference? = null
    fun save(): Task<Void> {
        doc = Firebase.firestore.document("campsites/" + id)
        return doc!!.set(this)
    }


}