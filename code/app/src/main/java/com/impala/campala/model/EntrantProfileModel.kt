package com.impala.campala.model

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore

/**
 * An entrant profile, it shares the same userId as the User class connected to the device
 * But it is separate so that it can be disabled without banning the USER itself, just by removing the Entrant
 *
 * @property userId The UID of the User this profile belongs to
 * @property name Entrant's name
 * @property email Entrant's email
 * @property phoneNumber Entrant's phone number
 * @property enteredLotteries IDs of the lotteries this entrant has entered TODO: change data type once lotteries are merged
 */
data class EntrantProfileModel(
    var userId: String = "",
    var name: String = "",  // TODO: These aren't really being used in the app just yet
    var email: String = "",  // TODO: These aren't really being used in the app just yet
    var phoneNumber: String = "", // TODO: These aren't really being used in the app just yet
    var enteredLotteries: MutableList<String> = mutableListOf()
) {
    companion object {
        fun load(doc: DocumentSnapshot): EntrantProfileModel {
            return doc.toObject(EntrantProfileModel::class.java)!!
        }
    }

    @get:Exclude
    var doc: DocumentReference? = null
    fun save(): Task<Void> {
        doc = Firebase.firestore.document("entrants/" + userId)
        return doc!!.set(this)
    }
}
