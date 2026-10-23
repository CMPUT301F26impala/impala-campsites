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
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val maxOccupancy: Int = 0,
    val mapLocation: String = "", // probably pair of float for lat and longitude
    val imageUrl: String = "",
    val tags: MutableList<String> = mutableListOf(),
    val id: String = Uuid.random().toString()
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