package com.impala.campala.model

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore
import kotlin.uuid.ExperimentalUuidApi

/**
 * This shares the same User ID as the associated User and EntrantProfile objects, but this tracks specifically the campsite owner details
 * This enables us to disable/ban a campsite owner profile without banning the user as a whole.
 *
 * @param ownerId The ID of the owner
 * @param entrantId The ID of the entrant reserving
 * @param reservationId The ID of the reservation spawning the chat, also used as primary ID
 */

data class MessagesModel @OptIn(ExperimentalUuidApi::class) constructor(
    val ownerId: String = "",
    val entrantId: String = "",
    val reservationId: String = "",
    var messageThread: MutableList<MessageModel> = mutableListOf(),
) {
    companion object {
        fun load(doc: DocumentSnapshot): MessagesModel {
            return doc.toObject(MessagesModel::class.java)!!
        }
    }

    @get:Exclude
    var doc: DocumentReference? = null
    /**
     * Saves the messages object in the Firebase messages collection
     *
     * @return A reference to the Firebase document of this messages model
     */
    fun save(): Task<Void> {
        doc = Firebase.firestore.document("messages/$reservationId")
        return doc!!.set(this)
    }
}