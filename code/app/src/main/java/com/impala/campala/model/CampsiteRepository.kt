package com.impala.campala.model

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.impala.campala.ObservableModel
import kotlinx.coroutines.tasks.await
/**
 * Repository for storing the app's campsites.
 *
 * Model for anything in the app that wants to access the campsite info
 * including add/update/delete functionality
 *
 * @property _campsites Read-only list of all campsites, useful for views that browse campsites
 * @property db Firestore database reference
 * @property campsitesRef Reference to the campsites collection in firebase
 */
class CampsiteRepository: ObservableModel<CampsiteRepository>() {
    private val _campsites = mutableStateListOf<CampsiteModel>()
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")
    // Taken/modified from lab 05
    init{
        campsitesRef.addSnapshotListener { snapshot, error ->
            if(error != null){
                return@addSnapshotListener
            }

            _campsites.clear()

            snapshot?.documents?.forEach { document ->
                val camp = document.toObject(CampsiteModel::class.java)
                if (camp != null){
                    _campsites.add(camp)
                }

            }
        }
    }

    val campsites: List<CampsiteModel>
        get() = _campsites

    // When a screen updates a campsite, it's controller will end up calling this method
    // with the new modifier Campsite object, then go and update the one with a matching
    // ID. If the campsite didn't exist before (eg: you just went like Campsite()
    // to create a new one) it will create it
    fun saveCampsite(campsite: CampsiteModel): Unit {
        campsite.save()
    }


    /**
     * Deletes Campsite from database
     */
    fun deleteCampsite(campsite: CampsiteModel): Unit {
        campsitesRef.document(campsite.id).delete()
    }
}