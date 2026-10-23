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
    var id: String = Uuid.random().toString()
) {
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