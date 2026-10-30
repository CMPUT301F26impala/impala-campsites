package com.impala.campala

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryModel.JoinResult
import com.impala.campala.model.LotteryRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Full disclaimer: These tests were primarily generated using Claude Opus 5.5 on Oct 29 2026
// I passed in the LotteryModel and LotteryRepository files as input with the prompt
// "Attached are a model and repository file for a lottery class, can you generate android JUnit test code to cover all the cases you can think of for getting and modifying lotteries"
// I then fully audited the file to ensure that all the cases I wanted were included, and I also ran the tests myself to ensure everything worked.
// If we want to in the future I can rewrite these by hand, but I felt that test code is very formulaic and was the perfect target to use AI-Assisted development on.
@RunWith(AndroidJUnit4::class)
class LotteryRepositoryTest {
    // Whole seconds only, Firestore stores timestamps at microsecond precision
    // so Timestamp.now() wouldn't come back exactly equal
    val lottery = LotteryModel(
        mutableListOf("azkaban"),
        Timestamp(1_000, 0),
        Timestamp(2_000, 0),
        Timestamp(3_000, 0),
        Timestamp(4_000, 0),
        2,
        mutableListOf(),
        1,
        mutableListOf(),
        "dumbledore",
        "test-lottery"
    )
    val repo = LotteryRepository()

    @Before
    fun setUp() {
        val db = FirebaseFirestore.getInstance()

        // Clear out the lotteries collection so every test starts from an empty database
        val snapshot = Tasks.await(db.collection("lotteries").get())

        val batch = db.batch()
        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }

        Tasks.await(batch.commit())
    }

    @Test
    fun createLottery(): Unit = runBlocking {
        repo.saveLottery(lottery)

        val stored = repo.getLotteryById(lottery.id)
        assertNotNull("Lottery should exist after saving", stored)
        assertEquals(lottery, stored)
    }

    @Test
    fun getLotteryByIdMissing(): Unit = runBlocking {
        assertNull("Getting a lottery that was never saved should return null", repo.getLotteryById("does-not-exist"))
    }

    @Test
    fun getLotteries(): Unit = runBlocking {
        assertEquals(0, repo.getLotteries().size)
        repo.saveLottery(lottery)
        repo.saveLottery(lottery.copy(campsiteIds = mutableListOf("hogwarts"), id = "other-lottery"))

        assertEquals(2, repo.getLotteries().size)
    }

    @Test
    fun getLotteryByCampsiteId(): Unit = runBlocking {
        val other = lottery.copy(campsiteIds = mutableListOf("hogwarts", "gringotts"), id = "other-lottery")
        repo.saveLottery(lottery)
        repo.saveLottery(other)

        assertEquals(lottery, repo.getLotteryByCampsiteId("azkaban"))
        assertEquals(other, repo.getLotteryByCampsiteId("hogwarts"))
        assertEquals(other, repo.getLotteryByCampsiteId("gringotts"))
        assertNull(repo.getLotteryByCampsiteId("no-match"))
    }

    @Test
    fun getLotteriesByOwnerId(): Unit = runBlocking {
        repo.saveLottery(lottery)
        repo.saveLottery(lottery.copy(id = "other-lottery"))
        repo.saveLottery(lottery.copy(ownerId = "voldemort", id = "evil-lottery"))

        assertEquals(2, repo.getLotteriesByOwnerId("dumbledore").size)
        assertEquals(listOf("evil-lottery"), repo.getLotteriesByOwnerId("voldemort").map { it.id })
        assertEquals(0, repo.getLotteriesByOwnerId("no-match").size)
    }

    @Test
    fun deleteLottery(): Unit = runBlocking {
        repo.saveLottery(lottery)
        assertNotNull("Lottery should exist before deleting", repo.getLotteryById(lottery.id))

        repo.deleteLottery(lottery)
        assertNull("Lottery should not exist after deleting", repo.getLotteryById(lottery.id))
    }

    @Test
    fun saveRejectsInvalidLottery() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repo.saveLottery(lottery.copy(ownerId = "")) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repo.saveLottery(lottery.copy(campsiteIds = mutableListOf())) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repo.saveLottery(lottery.copy(registrationEndTime = Timestamp(500, 0))) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repo.saveLottery(lottery.copy(reservationEndDate = Timestamp(2_500, 0))) }
        }
    }

    @Test
    fun joinFillsLotteryThenWaitlist(): Unit = runBlocking {
        repo.saveLottery(lottery)

        assertEquals(JoinResult.JOINED_LOTTERY, repo.joinLottery(lottery.id, "harry"))
        assertEquals(JoinResult.ALREADY_JOINED, repo.joinLottery(lottery.id, "harry"))
        assertEquals(JoinResult.JOINED_LOTTERY, repo.joinLottery(lottery.id, "ron"))
        assertEquals(JoinResult.JOINED_WAITLIST, repo.joinLottery(lottery.id, "hermione"))
        assertEquals(JoinResult.ALREADY_JOINED, repo.joinLottery(lottery.id, "hermione"))
        assertEquals(JoinResult.FULL, repo.joinLottery(lottery.id, "neville"))

        val stored = repo.getLotteryById(lottery.id)!!
        assertEquals(listOf("harry", "ron"), stored.entrantIds)
        assertEquals(listOf("hermione"), stored.waitlistIds)
    }

    @Test
    fun leavePromotesEarliestWaitlistEntrant(): Unit = runBlocking {
        val full = lottery.copy(
            entrantIds = mutableListOf("harry", "ron"),
            waitlistCapacity = 2,
            waitlistIds = mutableListOf("hermione", "neville")
        )
        repo.saveLottery(full)

        assertTrue(repo.leaveLottery(full.id, "harry"))

        val stored = repo.getLotteryById(full.id)!!
        assertEquals(listOf("ron", "hermione"), stored.entrantIds)
        assertEquals(listOf("neville"), stored.waitlistIds)
    }

    @Test
    fun leaveFromWaitlistDoesNotPromote(): Unit = runBlocking {
        val full = lottery.copy(
            entrantIds = mutableListOf("harry", "ron"),
            waitlistIds = mutableListOf("hermione")
        )
        repo.saveLottery(full)

        assertTrue(repo.leaveLottery(full.id, "hermione"))
        assertFalse(repo.leaveLottery(full.id, "neville"))

        val stored = repo.getLotteryById(full.id)!!
        assertEquals(listOf("harry", "ron"), stored.entrantIds)
        assertEquals(listOf<String>(), stored.waitlistIds)
    }
}
