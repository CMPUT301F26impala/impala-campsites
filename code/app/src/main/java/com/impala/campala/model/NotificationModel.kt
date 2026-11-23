package com.impala.campala.model

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore
import kotlin.uuid.Uuid

class NotificationModel(
    var title: String="",
    var body: String="",
    var sender: String="",
    var receivers: MutableList<String> = mutableListOf(),
    var sent: Boolean=false,
    var id: String = Uuid.random().toString()
) {
    companion object {
        fun load(doc: DocumentSnapshot): CampsiteModel {
            return doc.toObject(CampsiteModel::class.java)!!
        }
    }

    @get:Exclude
    var doc: DocumentReference? = null
    /**
     * Saves the object in the Firebase collection
     *
     * @return A reference to the Firebase document of this campsite model
     */
    fun save(): Task<Void> {
        doc = Firebase.firestore.document("notifications/" + id)
        return doc!!.set(this)
    }
}