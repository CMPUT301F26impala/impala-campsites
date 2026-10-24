package com.impala.campala

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CampsiteModelTest {
    val campsite = CampsiteModel(
        "azkaban",
        "prison",
        "bad place",
        69420,
        "TBD",
        "",
        mutableListOf("slytherins welcome")
    )
    val repo = CampsiteRepository()

    /**
     * Clears the campsite database before each test
     *
     * (Gemini, "how to clear the database for testing?", 2026-10-24)
     */
    @Before
    suspend fun clearCampsitesCollection() {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("campsites").get().await()

        val batch = db.batch()
        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }
        batch.commit().await()
    }

    @Test
    fun firestoreEmulatorRoundTrip() {

        Tasks.await(campsite.save(), 10, TimeUnit.SECONDS)

        val storedModel = CampsiteModel.load(
            Tasks.await(
                Firebase.firestore.collection("campsites").document(campsite.id).get()
            )
        )

        assertEquals(campsite, storedModel)
    }

    @Test
    fun repositoryFirestoreEmulatorAddDelete(){
        repo.saveCampsite(campsite)
        assert(repo.deleteCampsite(campsite))
    }
}