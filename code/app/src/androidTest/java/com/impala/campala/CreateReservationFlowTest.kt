package com.impala.campala

import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import com.impala.campala.model.ReservationRepository
import com.impala.campala.ui.theme.AppTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        // Fresh anonymous user, the app will pick this user up when it starts since they're already signed in
        Firebase.auth.signOut()
        uid = Firebase.auth.signInAnonymously().await().user!!.uid

        // Saying they already have an entrant profile skips the choose role screen
        Firebase.firestore.collection("users").document(uid).set(
            mapOf(
                "isEntrant" to true,
                "hasCreatedEntrantProfile" to true,
                "hasActiveEntrantProfile" to true,
                "currentRole" to UserViewMode.ENTRANT.name
            )
        ).await()

        CampsiteRepository().saveCampsite(campsite)

        // A lottery that already drew (with this user as the winner), registration is open right now,
        // and the stay window is next month so the date picker opens on a month we know
        val today = LocalDate.now(ZoneOffset.UTC)
        stayStart = today.plusMonths(1).withDayOfMonth(1)
        LotteryRepository().saveLottery(
            LotteryModel(
                campsiteIds = mutableListOf(campsite.id),
                lotteryStartTime = toTimestamp(today.minusDays(10)),
                lotteryEndTime = toTimestamp(today.minusDays(1)),
                registrationStartTime = toTimestamp(today.minusDays(1)),
                registrationEndTime = toTimestamp(today.plusDays(10)),
                reservationStartDate = toTimestamp(stayStart),
                reservationEndDate = toTimestamp(stayStart.plusDays(20)),
                lotteryCapacity = 1,
                // The user has to be in entrantIds too, since that's how the Lotteries tab finds their lotteries
                entrantIds = mutableListOf(uid),
                winnerIds = mutableListOf(uid),
                ownerId = "ui-test-owner"
            )
        )
    }

    @Test
    fun winnerCanReserveFromLotteriesTab() {
        compose.setContent { AppTheme { CampalaApp() } }

        // Click on lotteries tab
        waitForText("Lotteries")
        compose.onNodeWithText("Lotteries").performClick()

        // Click on reserve (assuming we've already won a lottery and it's here)
        waitForText("Reserve")
        compose.onNodeWithText("Reserve").performClick()

        // Accept terms
        waitForText("I accept")
        compose.onNodeWithText("I accept").performClick()

        // Pick dates
        val start = stayStart.plusDays(9)
        val end = stayStart.plusDays(11)
        pickDate("Start Date", start)
        pickDate("End Date", end)

        // I wasn't able to access the FAB, so again I prompted Claude Opus 5.5 and it told me to try useUnmergedTree
        waitForText("Confirm Reservation", useUnmergedTree = true)
        compose.onNodeWithText("Confirm Reservation", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Confirm").performClick()

        waitForText("You're All Set!")

        // Make sure the flow actually saved what the user picked
        val saved = runBlocking { ReservationRepository().getReservationsByUser(uid) }
        assertEquals(1, saved.size)
        assertEquals(campsite.id, saved[0].campsiteId)
        assertEquals(toTimestamp(start), saved[0].startDate)
        assertEquals(toTimestamp(end), saved[0].endDate)
    }

    /**
     * Opens a date field's picker, taps a day, and presses OK
     *
     * Day cells in the Material date picker are labelled with the full date (eg: "Thursday, December 10, 2026"),
     * and the screen behind the dialog also has day numbers on it, so this only looks inside the dialog
     */
    private fun pickDate(fieldLabel: String, date: LocalDate) {
        compose.onNodeWithText(fieldLabel).performScrollTo().performClick()
        val dayLabel = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US))
        val dayCell = hasText(dayLabel, substring = true) and hasClickAction() and hasAnyAncestor(isDialog())
        compose.waitUntil(TIMEOUT_MS) { compose.onAllNodes(dayCell).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(dayCell).performClick()
        compose.onNodeWithText("OK").performClick()

        // The field should now show the picked date
        waitForText(date.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")))
    }

    // Compose can't tell when Firestore calls finish, so anything that waits on data has to wait for it to show up
    private fun waitForText(text: String, useUnmergedTree: Boolean = false) {
        compose.waitUntil(TIMEOUT_MS) {
            compose.onAllNodesWithText(text, useUnmergedTree = useUnmergedTree).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // Same conversion the app uses, dates are stored as midnight UTC
    private fun toTimestamp(date: LocalDate): Timestamp {
        return Timestamp(date.atStartOfDay(ZoneOffset.UTC).toInstant())
    }

    companion object {
        private const val TIMEOUT_MS = 10_000L
    }
}
