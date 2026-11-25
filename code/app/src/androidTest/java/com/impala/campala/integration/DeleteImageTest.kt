package com.impala.campala.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import com.impala.campala.model.ImageModel
import com.impala.campala.model.ImageRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Tests for ImageRepository.deleteImageAndReferences
 *
 * Makes an image used by a campsite and an entrant's profile picture, plus another image that stays,
 * then deletes the first one and makes sure it's removed from both
 */
@OptIn(ExperimentalUuidApi::class)
@RunWith(AndroidJUnit4::class)
class DeleteImageTest {
    private val image = ImageModel(id = "delete-image-test-" + Uuid.random())
    private val otherImage = ImageModel(id = "delete-image-test-other-" + Uuid.random())

    private val campsite = CampsiteModel(name = "Delete Image Test Campsite", ownerId = "delete-image-owner", imageId = image.id)
    private val otherCampsite = CampsiteModel(name = "Delete Image Test Other Campsite", ownerId = "delete-image-owner", imageId = otherImage.id)

    private val entrant = EntrantProfileModel(userId = "delete-image-entrant-" + Uuid.random(), name = "Avery", imageId = image.id)
    private val otherEntrant = EntrantProfileModel(userId = "delete-image-entrant-" + Uuid.random(), name = "Jordan", imageId = otherImage.id)

    @Before
    fun setUp(): Unit = runBlocking {
        ImageRepository.saveImage(image)
        ImageRepository.saveImage(otherImage)
        CampsiteRepository.saveCampsite(campsite)
        CampsiteRepository.saveCampsite(otherCampsite)
        EntrantProfileRepository.saveEntrantProfile(entrant)
        EntrantProfileRepository.saveEntrantProfile(otherEntrant)

        ImageRepository.deleteImageAndReferences(image.id)
    }

    @After
    fun tearDown(): Unit = runBlocking {
        val db = Firebase.firestore
        val docs = listOf(
            "images/${image.id}",
            "images/${otherImage.id}",
            "campsites/${campsite.id}",
            "campsites/${otherCampsite.id}",
            "entrants/${entrant.userId}",
            "entrants/${otherEntrant.userId}"
        )
        for (path in docs) {
            db.document(path).delete().await()
        }
    }

    @Test
    fun imageIsDeleted(): Unit = runBlocking {
        assertNull(ImageRepository.getImageById(image.id))
        assertNotNull(ImageRepository.getImageById(otherImage.id))
    }

    @Test
    fun imageIsRemovedFromCampsite(): Unit = runBlocking {
        val stored = CampsiteRepository.getCampsiteById(campsite.id)
        assertEquals("", stored?.imageId)
        assertEquals(campsite.name, stored?.name)
        assertEquals(otherImage.id, CampsiteRepository.getCampsiteById(otherCampsite.id)?.imageId)
    }

    @Test
    fun imageIsRemovedFromProfilePicture(): Unit = runBlocking {
        val stored = EntrantProfileRepository.getEntrantProfileById(entrant.userId)
        assertEquals("", stored?.imageId)
        assertEquals(entrant.name, stored?.name)
        assertEquals(otherImage.id, EntrantProfileRepository.getEntrantProfileById(otherEntrant.userId)?.imageId)
    }
}
