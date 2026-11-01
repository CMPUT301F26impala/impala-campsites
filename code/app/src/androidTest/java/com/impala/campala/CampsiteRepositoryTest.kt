package com.impala.campala

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// TODO: getFilteredCampsitees IS NOT covered here yet
@RunWith(AndroidJUnit4::class)
class CampsiteRepositoryTest {
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
        repo.saveCampsite(campsite)

        val stored = repo.getCampsiteById(campsite.id)
        assertNotNull("Campsite should exist after saving", stored)
        assertEquals(campsite, stored)
    }

    @Test
    fun getCampsiteByIdMissing(): Unit = runBlocking {
        assertNull("Getting a campsite that was never saved should return null", repo.getCampsiteById("does-not-exist"))
    }

    @Test
    fun getCampsiteByIdPicksCorrectCampsite(): Unit = runBlocking {
        val other = campsite.copy(name = "hogwarts", id = "other-campsite")
        repo.saveCampsite(campsite)
        repo.saveCampsite(other)

        assertEquals(campsite, repo.getCampsiteById(campsite.id))
        assertEquals(other, repo.getCampsiteById(other.id))
    }

    @Test
    fun updateCampsite_savingWithSameIdOverwritesInsteadOfCreatingDuplicate(): Unit = runBlocking {
        repo.saveCampsite(campsite)

        val updated = campsite.copy(name = "azkaban 2", maxOccupancy = 5, tags = mutableListOf("dementors"))
        repo.saveCampsite(updated)

        assertEquals(updated, repo.getCampsiteById(campsite.id))
        assertEquals(1, repo.getFilteredCampsites().size)
    }

    @Test
    fun deleteCampsite(): Unit = runBlocking {
        repo.saveCampsite(campsite)
        assertNotNull("Campsite should exist before deleting", repo.getCampsiteById(campsite.id))

        repo.deleteCampsite(campsite)
        assertNull("Campsite should not exist after deleting", repo.getCampsiteById(campsite.id))
    }
}
