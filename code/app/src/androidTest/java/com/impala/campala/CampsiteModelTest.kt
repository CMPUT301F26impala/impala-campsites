package com.impala.campala

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
//    suspend fun clearCampsitesCollection() {
//        val db = FirebaseFirestore.getInstance()
//        val snapshot = db.collection("campsites").get().await()
//
//        val batch = db.batch()
//        for (document in snapshot.documents) {
//            batch.delete(document.reference)
//        }
//        batch.commit().await()
//    }

    @Before
    fun setUp() {
        val db = FirebaseFirestore.getInstance()

        // Tasks.await() blocks the thread synchronously until the query finishes
        val snapshot = Tasks.await(db.collection("campsites").get())

        val batch = db.batch()
        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }

        // Commit the batch synchronously too
        Tasks.await(batch.commit())
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
    fun repositoryAddDelete(){
        repo.saveCampsite(campsite)
        repo.deleteCampsite(campsite)
        val didDelete = Tasks.await(Firebase.firestore.collection("campsites").document(campsite.id).get())
        assertFalse("Document should no longer exist in Firestore after deletion", didDelete.exists())
    }

    @Test
    fun repositoryFirestoreEmulatorSave(){
        repo.deleteCampsite(campsite)
        repo.saveCampsite(campsite)

        val storedModel = CampsiteModel.load(
            Tasks.await(
                Firebase.firestore.collection("campsites").document(campsite.id).get()
            )
        )

        assertEquals(campsite, storedModel)
    }
}