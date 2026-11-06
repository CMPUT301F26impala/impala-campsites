package com.impala.campala.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.ReservationRepository
import com.impala.campala.testutil.clickText
import com.impala.campala.testutil.launchApp
import com.impala.campala.testutil.pickDate
import com.impala.campala.testutil.seedCampsite
import com.impala.campala.testutil.seedLottery
import com.impala.campala.testutil.signInAsNewEntrant
import com.impala.campala.testutil.toTimestamp
import com.impala.campala.testutil.todayUtc
import com.impala.campala.testutil.waitForText
import com.impala.campala.testutil.wonLottery
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * UI test for taking a lottery you already won and then doing the whole reserve flow
 *
 * It goes Lotteries tab -> Reserve -> accept terms -> pick dates -> confirm,
 * then checks that the reservation actually got saved to Firestore
 *
 * The data is seeded straight into the Firebase emulator before the app is rendered
 * Each test signs in as a brand new anonymous user, so tests can't see each other's lotteries/reservations
 */
@RunWith(AndroidJUnit4::class)
class CreateReservationFlowTest {
    // DISCLOSURE:
    // I used Claude Opus 5.5 to help me figure out how I would go about initializing the firestore emulator
    // I prompted it saying "I want to create automated UI tests, but some of them require test data already in firebase and connect to the test runner's user account"
    // It created the seed function below and helped me set up the scaffolding for this, including the waitForText() function and the timeout pieces
    // I then created the actual test function itself, choosing what steps

    // createComposeRule instead of createAndroidComposeRule<MainActivity>, because the activity rule
    // launches the app before @Before runs, which would be before the test data exists
    @get:Rule
    val compose = createComposeRule()

    private lateinit var uid: String
    private lateinit var stayStart: LocalDate

    // No fixed id, so every test gets a new random one
    // Otherwise reservations left over from an earlier run would already be booking the dates this test picks
    private val campsite = CampsiteModel(
        name = "Hogwarts Grounds",
        location = "Scotland",
        maxOccupancy = 4,
        rules = "No dragons"
    )

    @Before
    fun seed(): Unit = runBlocking {
        uid = signInAsNewEntrant()
        seedCampsite(campsite)

        // A lottery that already drew (with this user as the winner), registration is open right now,
        // and the stay window is next month so the date picker opens on a month we know
        stayStart = todayUtc().plusMonths(1).withDayOfMonth(1)
        seedLottery(wonLottery(campsite.id, winnerId = uid, stayStart = stayStart))
    }

    @Test
    fun winnerCanReserveFromLotteriesTab() {
        compose.launchApp()

        // Click on lotteries tab
        compose.clickText("Lotteries")

        // Click on reserve (assuming we've already won a lottery and it's here)
        compose.clickText("Reserve")

        // Accept terms
        compose.clickText("I accept")

        // Pick dates
        val start = stayStart.plusDays(9)
        val end = stayStart.plusDays(11)
        compose.pickDate("Start Date", start)
        compose.pickDate("End Date", end)

        // I wasn't able to access the FAB, so again I prompted Claude Opus 5.5 and it told me to try useUnmergedTree
        compose.clickText("Confirm Reservation", useUnmergedTree = true)
        compose.onNodeWithText("Confirm").performClick()

        compose.waitForText("You're All Set!")

        // Make sure the flow actually saved what the user picked
        val saved = runBlocking { ReservationRepository().getReservationsByUser(uid) }
        assertEquals(1, saved.size)
        assertEquals(campsite.id, saved[0].campsiteId)
        assertEquals(toTimestamp(start), saved[0].startDate)
        assertEquals(toTimestamp(end), saved[0].endDate)
    }
}
