package com.impala.campala

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.impala.campala.model.LotteryRepository
import com.impala.campala.testutil.assertTextStaysHidden
import com.impala.campala.testutil.clearFirestore
import com.impala.campala.testutil.clickContentDescription
import com.impala.campala.testutil.clickText
import com.impala.campala.testutil.launchApp
import com.impala.campala.testutil.signInAsNewEntrant
import com.impala.campala.testutil.switchToEntrant
import com.impala.campala.testutil.switchToOwner
import com.impala.campala.testutil.typeDate
import com.impala.campala.testutil.typeInto
import com.impala.campala.testutil.waitForDatabase
import com.impala.campala.testutil.waitForText
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * UI test for one user going back and forth between the entrant and campsite owner sides,
 * checking that what the owner does shows up for entrants
 *
 *  1. In entrant, there are no campsites
 *  2. Go to owner, create a campsite
 *  3. Back to entrant, the campsite shows up
 *  4. Back to owner, edit the campsite
 *  5. Back to entrant, the edits show up
 *  6. Back to owner, create a lottery for the campsite
 *  7. Back to entrant, join the lottery
 *  8. Switch to the lotteries tab and see it there
 *
 * The database is wiped first, so step 1 really does start with no campsites
 */
@RunWith(AndroidJUnit4::class)
class OwnerToEntrantFlowTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var uid: String

    @Before
    fun seed(): Unit = runBlocking {
        clearFirestore()
        uid = signInAsNewEntrant()
    }

    @Test
    fun ownerChangesShowUpForEntrant() {
        compose.launchApp()

        // 1
        compose.waitForText("Search")
        compose.assertTextStaysHidden("People", substring = true)

        // 2
        compose.switchToOwner()
        compose.clickText("Add Campsite", useUnmergedTree = true)
        compose.waitForText("Create Campsite")
        compose.typeInto("Campsite Title", "Hogwarts Grounds")
        compose.typeInto("Campsite Location", "Scotland")
        compose.typeInto("Campsite Description", "Castle grounds by the lake")
        compose.typeInto("Maximum Occupancy", "4")
        compose.clickText("Create")
        compose.waitForText("Add Campsite", useUnmergedTree = true)
        compose.waitForText("Hogwarts Grounds")

        // 3
        compose.switchToEntrant()
        compose.waitForText("Hogwarts Grounds")
        compose.waitForText("Scotland")
        compose.waitForText("4 People")

        // 4
        compose.switchToOwner()
        compose.clickText("Hogwarts Grounds")
        compose.clickContentDescription("Edit Icon")
        compose.waitForText("Castle grounds by the lake")
        compose.typeInto("Campsite Title", "Hogsmeade Meadow")
        compose.typeInto("Campsite Location", "Highlands")
        compose.typeInto("Maximum Occupancy", "6")
        compose.clickText("Save")
        compose.waitForText("Up to 6 occupants")
        compose.clickContentDescription("Back Icon")

        // 5
        compose.switchToEntrant()
        compose.waitForText("Hogsmeade Meadow")
        compose.waitForText("Highlands")
        compose.waitForText("6 People")
        compose.onAllNodesWithText("Hogwarts Grounds").assertCountEquals(0)

        // 6
        compose.switchToOwner()
        compose.clickText("Lotteries")
        compose.clickText("Add Lottery", useUnmergedTree = true)
        compose.waitForText("Create Lottery")
        compose.clickText("Select Campsite")
        compose.clickText("Hogsmeade Meadow")
        compose.typeInto("Maximum Participants", "10")
        compose.typeInto("Waitlist Size", "5")
        compose.typeInto("Number of Winners", "2")
        val today = LocalDate.now()
        val joinEnd = today.plusDays(7)
        compose.typeDate("Start Date", today.minusDays(1))
        compose.typeDate("End Date", joinEnd)
        compose.typeDate("Start Date", today.plusDays(8))
        compose.typeDate("End Date", today.plusDays(14))
        compose.typeDate("Start Date", today.plusDays(30))
        compose.typeDate("End Date", today.plusDays(40))
        compose.clickText("Create")
        compose.waitForText("Add Lottery", useUnmergedTree = true)
        compose.waitForText("Hogsmeade Meadow")

        // 7
        compose.switchToEntrant()
        compose.clickText("Hogsmeade Meadow")
        compose.clickText("Join Lottery")
        compose.waitForDatabase { LotteryRepository().getLotteriesByEntrantId(uid).isNotEmpty() }
        compose.clickContentDescription("Back Icon")
        compose.clickText("Lotteries")
        compose.waitForText("Hogsmeade Meadow")
        compose.waitForText("Draws on " + joinEnd.format(DateTimeFormatter.ofPattern("MMM d, yyyy")))

        val joined = runBlocking { LotteryRepository().getLotteriesByEntrantId(uid) }
        assertEquals(1, joined.size)
        assertEquals(1, joined[0].campsiteIds.size)
        assertEquals(10, joined[0].lotteryCapacity)
        assertEquals(listOf(uid), joined[0].entrantIds)
    }
}
