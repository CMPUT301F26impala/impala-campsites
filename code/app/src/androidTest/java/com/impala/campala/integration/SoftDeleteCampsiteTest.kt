package com.impala.campala.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteOwnerProfileModel
import com.impala.campala.model.CampsiteOwnerProfileRepository
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.ImageModel
import com.impala.campala.model.ImageRepository
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import com.impala.campala.model.ReservationModel
import com.impala.campala.model.ReservationRepository
import com.impala.campala.model.ReviewModel
import com.impala.campala.model.ReviewRepository
import com.impala.campala.testutil.openLottery
import com.impala.campala.testutil.toTimestamp
import com.impala.campala.testutil.todayUtc
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Tests for CampsiteRepository.softDeleteCampsite
 *
 * Generates a bunch of example data (some of which was generated with the help of Claude Opus 5.5, asking "Can you generate example ___ to fit into this campsite so I can test deletion")
 * And then it deletes the campsite and makes sure the relevant other pieces of data were also changed
 */
@OptIn(ExperimentalUuidApi::class)
@RunWith(AndroidJUnit4::class)
class SoftDeleteCampsiteTest {
    private val ownerId = "soft-delete-owner-" + Uuid.random()
    private val image = ImageModel(id = "soft-delete-image-" + Uuid.random())
    private val campsite = CampsiteModel(
        name = "Soft Delete Test Campsite " + Uuid.random(),
        location = "Banff",
        description = "Forested sites",
        maxOccupancy = 6,
        imageId = image.id,
        tags = mutableListOf("Forest"),
        rules = "Pets must be leashed",
        ratingTotal = 9.0,
        ratingCount = 2,
        ownerId = ownerId
    )
    private val otherCampsite = CampsiteModel(name = "Soft Delete Test Other Campsite", ownerId = ownerId)

    private val today = todayUtc()
    private val onlyLottery = openLottery(listOf(campsite.id), ownerId)
    private val sharedLottery = openLottery(listOf(campsite.id, otherCampsite.id), ownerId)
    private val finishedLottery = openLottery(listOf(campsite.id), ownerId).copy(
        lotteryStartTime = toTimestamp(today.minusDays(30)),
        lotteryEndTime = toTimestamp(today.minusDays(20)),
        registrationStartTime = toTimestamp(today.minusDays(20)),
        registrationEndTime = toTimestamp(today.minusDays(10))
    )

    private val upcomingReservation = reservation(startDaysFromToday = 10, endDaysFromToday = 12)
    private val ongoingReservation = reservation(startDaysFromToday = -1, endDaysFromToday = 1)
    private val pastReservation = reservation(startDaysFromToday = -12, endDaysFromToday = -10)

    private val review = ReviewModel(posterId = "soft-delete-entrant", campsiteId = campsite.id, rating = 4.0, reviewText = "Nice")
    private val otherReview = ReviewModel(posterId = "soft-delete-entrant", campsiteId = otherCampsite.id, rating = 5.0, reviewText = "Great")

    private fun reservation(startDaysFromToday: Long, endDaysFromToday: Long) = ReservationModel(
        campsiteId = campsite.id,
        ownerId = ownerId,
        entrantId = "soft-delete-entrant",
        startDate = toTimestamp(today.plusDays(startDaysFromToday)),
        endDate = toTimestamp(today.plusDays(endDaysFromToday))
    )

    @Before
    fun setUp(): Unit = runBlocking {
        ImageRepository.saveImage(image)
        CampsiteRepository.saveCampsite(campsite)
        CampsiteRepository.saveCampsite(otherCampsite)
        CampsiteOwnerProfileRepository.saveCampsiteOwnerProfile(
            CampsiteOwnerProfileModel(userId = ownerId, ownedCampsites = mutableListOf(campsite.id, otherCampsite.id))
        )
        for (lottery in listOf(onlyLottery, sharedLottery, finishedLottery)) {
            LotteryRepository.saveLottery(lottery)
        }
        for (reservation in listOf(upcomingReservation, ongoingReservation, pastReservation)) {
            ReservationRepository.saveReservation(reservation)
        }
        ReviewRepository.saveReview(review)
        ReviewRepository.saveReview(otherReview)

        CampsiteRepository.softDeleteCampsite(campsite)
    }

    @After
    fun tearDown(): Unit = runBlocking {
        val db = Firebase.firestore
        val docs = listOf(
            "images/${image.id}",
            "campsites/${campsite.id}",
            "campsites/${otherCampsite.id}",
            "campsiteOwners/$ownerId",
            "lotteries/${onlyLottery.id}",
            "lotteries/${sharedLottery.id}",
            "lotteries/${finishedLottery.id}",
            "reservations/${upcomingReservation.id}",
            "reservations/${ongoingReservation.id}",
            "reservations/${pastReservation.id}",
            "reviews/${review.id}",
            "reviews/${otherReview.id}"
        )
        for (path in docs) {
            db.document(path).delete().await()
        }
    }

    @Test
    fun campsiteIsKeptButAnonymised(): Unit = runBlocking {
        val stored = CampsiteRepository.getCampsiteById(campsite.id)
        assertNotNull("The campsite document should be kept", stored)
        stored!!

        assertTrue(stored.hasBeenDeleted)
        assertEquals("Deleted Campsite", stored.name)
        assertEquals("", stored.location)
        assertEquals("", stored.description)
        assertEquals("", stored.rules)
        assertEquals("", stored.imageId)
        assertTrue(stored.tags.isEmpty())
        assertEquals(ownerId, stored.ownerId)
    }

    @Test
    fun campsiteIsHiddenFromSearch(): Unit = runBlocking {
        val ids = CampsiteRepository.getFilteredCampsites().map { it.id }
        assertFalse(campsite.id in ids)
        assertTrue(otherCampsite.id in ids)
    }

    @Test
    fun imageIsDeleted(): Unit = runBlocking {
        assertNull(ImageRepository.getImageById(image.id))
    }

    @Test
    fun campsiteIsRemovedFromOwnerProfile(): Unit = runBlocking {
        val profile = CampsiteOwnerProfileRepository.getCampsiteOwnerProfileById(ownerId)
        assertEquals(listOf(otherCampsite.id), profile?.ownedCampsites)
    }

    @Test
    fun unfinishedLotteryWithOnlyThisCampsiteIsDeleted(): Unit = runBlocking {
        assertNull(LotteryRepository.getLotteryById(onlyLottery.id))
    }

    @Test
    fun campsiteIsRemovedFromSharedLottery(): Unit = runBlocking {
        val stored = LotteryRepository.getLotteryById(sharedLottery.id)
        assertEquals(listOf(otherCampsite.id), stored?.campsiteIds)
    }

    @Test
    fun finishedLotteryIsKept(): Unit = runBlocking {
        val stored = LotteryRepository.getLotteryById(finishedLottery.id)
        assertEquals(listOf(campsite.id), stored?.campsiteIds)
    }

    @Test
    fun reservationsThatHaveNotEndedAreCancelled(): Unit = runBlocking {
        assertTrue(ReservationRepository.getReservationById(upcomingReservation.id)!!.hasBeenCancelled)
        assertTrue(ReservationRepository.getReservationById(ongoingReservation.id)!!.hasBeenCancelled)
    }

    @Test
    fun pastReservationIsKept(): Unit = runBlocking {
        val stored = ReservationRepository.getReservationById(pastReservation.id)
        assertNotNull(stored)
        assertFalse(stored!!.hasBeenCancelled)
    }

    @Test
    fun onlyThisCampsitesReviewsAreDeleted(): Unit = runBlocking {
        assertTrue(ReviewRepository.getReviewsByCampsite(campsite.id).isEmpty())
        assertEquals(listOf(otherReview), ReviewRepository.getReviewsByCampsite(otherCampsite.id))
    }
}
