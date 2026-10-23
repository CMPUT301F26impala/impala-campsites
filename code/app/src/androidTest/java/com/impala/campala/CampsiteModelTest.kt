package com.impala.campala

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.impala.campala.model.CampsiteModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CampsiteModelTest {
    @Test
    fun firestoreEmulatorRoundTrip() {

        val campsite = CampsiteModel(
            "azkaban",
            "prison",
            "bad place",
            69420,
            "TBD",
            "",
            mutableListOf("slytherins welcome")
        )

        Tasks.await(campsite.save(), 10, TimeUnit.SECONDS)

        val storedModel = CampsiteModel.load(
            Tasks.await(
                Firebase.firestore.collection("campsites").document(campsite.id).get()
            )
        )

        assertEquals(campsite, storedModel)
    }
}