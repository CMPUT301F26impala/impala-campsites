package com.impala.campala.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CampsiteRepositoryTest {
    val campsite = CampsiteModel(
        "azkaban",
        "prison",
        "bad place",
        69420,
        null,
        "",
        mutableListOf("slytherins welcome")
    )

    @Before
    fun setUp() {
        val db = FirebaseFirestore.getInstance()

        // Clear out the campsites collection so every test starts from an empty database
        val snapshot = Tasks.await(db.collection("campsites").get())

        val batch = db.batch()
        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }

        Tasks.await(batch.commit())
    }

    @Test
    fun createCampsite(): Unit = runBlocking {
        CampsiteRepository.saveCampsite(campsite)

        val stored = CampsiteRepository.getCampsiteById(campsite.id)
        assertNotNull("Campsite should exist after saving", stored)
        assertEquals(campsite, stored)
    }

    @Test
    fun getCampsiteByIdMissing(): Unit = runBlocking {
        assertNull("Getting a campsite that was never saved should return null", CampsiteRepository.getCampsiteById("does-not-exist"))
    }

    @Test
    fun getCampsiteByIdPicksCorrectCampsite(): Unit = runBlocking {
        val other = campsite.copy(name = "hogwarts", id = "other-campsite")
        CampsiteRepository.saveCampsite(campsite)
        CampsiteRepository.saveCampsite(other)

        assertEquals(campsite, CampsiteRepository.getCampsiteById(campsite.id))
        assertEquals(other, CampsiteRepository.getCampsiteById(other.id))
    }

    @Test
    fun updateCampsite_savingWithSameIdOverwritesInsteadOfCreatingDuplicate(): Unit = runBlocking {
        CampsiteRepository.saveCampsite(campsite)

        val updated = campsite.copy(name = "azkaban 2", maxOccupancy = 5, tags = mutableListOf("dementors"))
        CampsiteRepository.saveCampsite(updated)

        assertEquals(updated, CampsiteRepository.getCampsiteById(campsite.id))
        assertEquals(1, CampsiteRepository.getFilteredCampsites().size)
    }

    @Test
    fun deleteCampsite(): Unit = runBlocking {
        CampsiteRepository.saveCampsite(campsite)
        assertNotNull("Campsite should exist before deleting", CampsiteRepository.getCampsiteById(campsite.id))

        CampsiteRepository.deleteCampsite(campsite)
        assertNull("Campsite should not exist after deleting", CampsiteRepository.getCampsiteById(campsite.id))
    }

    // Moved here from the old androidTest CampsiteModelTest, since these go through Firestore
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
    fun repositorySearchString(): Unit = runBlocking {
        CampsiteRepository.saveCampsite(campsite)
        var campsites = CampsiteRepository.getFilteredCampsites("No match",null,null)
        assertEquals(0,campsites.size)
        campsites = CampsiteRepository.getFilteredCampsites("azkaban",null,null)
        assertEquals(1,campsites.size)
        campsites = CampsiteRepository.getFilteredCampsites("slytherins welcome",null,null)
        assertEquals(1,campsites.size)
    }

    @Test
    fun repositoryCapacity(): Unit = runBlocking {
        CampsiteRepository.saveCampsite(campsite)
        var campsites = CampsiteRepository.getFilteredCampsites(null,null,69420)
        assertEquals(1,campsites.size)
        campsites = CampsiteRepository.getFilteredCampsites(null,null,1)
        assertEquals(1,campsites.size)
        campsites = CampsiteRepository.getFilteredCampsites(null,null,69421)
        assertEquals(0,campsites.size)
    }
}
