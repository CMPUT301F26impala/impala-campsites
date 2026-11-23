package com.impala.campala.model

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Instead of using the paid file uploading service for Firestore, this stores images as Webp Blobs in Firestore.
 * It is assumed that images are pre-compressed before being saved here.
 *
 * @param data The compressed image bytes from ImageCompressor
 * @param id The ID of the image connecting it to an entry in the database
 *
 * @property doc A reference to the Firebase document containing this object
 */
data class ImageModel @OptIn(ExperimentalUuidApi::class) constructor(
    var data: Blob? = null,
    var id: String = Uuid.random().toString()
) {
    companion object {
        fun load(doc: DocumentSnapshot): ImageModel {
            return doc.toObject(ImageModel::class.java)!!
        }
    }

    @get:Exclude
    var doc: DocumentReference? = null

    /**
     * Saves the image object in the Firebase images collection
     *
     * @return A Task that completes when the image has been written to the database
     */
    fun save(): Task<Void> {
        doc = Firebase.firestore.document("images/" + id)
        return doc!!.set(this)
    }
}
