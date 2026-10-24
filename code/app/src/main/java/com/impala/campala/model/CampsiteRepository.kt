package com.impala.campala.model

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.impala.campala.ObservableModel

/**
 * Repository for storing the app's campsites.
 *
 * Model for anything in the app that wants to access the campsite info
 * including add/update/delete functionality
 *
 * @property db Firestore database lazy reference
 */
class CampsiteRepository: ObservableModel<CampsiteRepository>() {
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    /**
     * Gets a list of campsites based on optional filters
     *
     * Used some code help from Gemini for querying
     * (Google Gemini, "if i want one of the input string to match either the name or any of the
     *  tags and it could be null, how would I? also the string is case sensitive and can match
     *  substrings", 2026-10-24)
     *
     * All inputs are nullable as to make it possible for controllers to input what filters
     * including no filters to get all campsites
     *
     * @param searchString Can match tags or name, case-insensitive
     * @param location TODO: change data type
     * @param mapLocation TODO: Change data type
     * @param numPeople Filters for campsites with capacity for numPeople
     *
     * @return List of matching campsites
     */
    fun getFilteredCampsites(searchString: String?, location: String?,
                             mapLocation: String?,numPeople: Int?): List<CampsiteModel>{
        var query: Query = db.collection("campsites")

        if(location!=null){
            query = query.whereEqualTo("location",location)
        }
        if(mapLocation!=null){
            query = query.whereEqualTo("mapLocation",mapLocation)
        }
        if(numPeople!=null){
            query = query.whereGreaterThanOrEqualTo("maxOccupancy",numPeople)
        }


        // Done db querying, search string filtering is done with output
        val snapshot = Tasks.await(query.get())
        val allCampsites = snapshot.toObjects(CampsiteModel::class.java)
        if (searchString.isNullOrBlank()) { // No search, just return
            return allCampsites
        }
        return allCampsites.filter { campsite ->
            val matchesName = campsite.name.contains(searchString, ignoreCase = true)
            val matchesTags = campsite.tags.any { tag -> tag.contains(searchString, ignoreCase = true) }
            matchesName || matchesTags
        }
    }

    /**
     * Saves a campsite to the database
     *
     *  When a screen updates a campsite, it's controller will end up calling this method
     *      with the new modifier Campsite object, then go and update the one with a matching
     *      ID. If the campsite didn't exist before (eg: you just went like Campsite()
     *      to create a new one) it will create it
     *
     * @param campsite CampsiteModel to be saved
    */
    fun saveCampsite(campsite: CampsiteModel): Unit {
        campsite.save()
    }

    /**
     * Deletes Campsite from database
     *
     * @param campsite CampsiteModel to delete
     */
    fun deleteCampsite(campsite: CampsiteModel): Unit {
        db.collection("campsites").document(campsite.id).delete()
    }
}