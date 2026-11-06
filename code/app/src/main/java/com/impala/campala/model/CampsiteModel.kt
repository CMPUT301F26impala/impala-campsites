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
 * The model of a campsite containing all the necessary details
 * The object will be loaded directly from the database
 *
 * @param name Name of the campsite
 * @param location String name of the campsite location
 * @param description Campsite description as a String
 * @param maxOccupancy Integer for the campsite's maximum occupancy
 * @param mapLocation TODO: Implement this as likely a pair of floats with coordinates
 * @param imageUrl Link to the profile picture of the campsite
 * @param tags A list of Strings with relevant tags
 * @param rules A string containing campsite rules
 * @param id The ID of the campsite connecting it to an entry in the database
 *
 * @property doc A reference to the Firebase document containing this object
 */
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
    /**
     * Saves the campsite object in the Firebase campsites collection
     *
     * @return A reference to the Firebase document of this campsite model
     */
    fun save(): Task<Void> {
        doc = Firebase.firestore.document("campsites/" + id)
        return doc!!.set(this)
    }


}