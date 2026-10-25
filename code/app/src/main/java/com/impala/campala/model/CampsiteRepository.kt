package com.impala.campala.model

import android.util.Log
import androidx.compose.runtime.remember
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.impala.campala.ObservableModel
import kotlinx.coroutines.tasks.await

/**
 * Repository for storing the app's campsites.
 *
 * Model for anything in the app that wants to access the campsite info
 * including add/update/delete functionality
 *
 * @property db Firestore database lazy reference
 */
class CampsiteRepository {
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    // Things that use Task.await() can't run on the main thread
    // I figured this out after doing debugging with Claude Opus 5.5 asking why I was getting UI crashes and pasting in the original version of this function in
    // The solution I settled on was to make this a suspend fun, and then where it's called in the app it just needs to be inside of a coroutine scope.

    // You can do this from any @Composable function by doing rememberCoroutineScope() and then using scope.launch
    // Or you can use LaunchedEffect(Unit) if you want something to run as soon as that Composable function is rendered
    // If you want to use it inside a model class, you just have to pass in the coroutine scope as a parameter. So we could create the scope inside the root of our app and pass it to our classes

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
     * @param authorId TODO: The Campsite Owner ID of the creator, used so that campsite owners can filter to only their own campsites
     *
     * Suspends instead of blocking, so it's safe to call from the main thread
     * as long as it's inside a coroutine (eg: LaunchedEffect or rememberCoroutineScope in Compose)
     *
     * @return List of matching campsites
     */
    suspend fun getFilteredCampsites(searchString: String? = null, location: String? = null, mapLocation: String? = null, numPeople: Int? = null, campsiteOwnerId: String? = null): List<CampsiteModel>{
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
        val snapshot = query.get().await()
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