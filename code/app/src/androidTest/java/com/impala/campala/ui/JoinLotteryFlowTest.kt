package com.impala.campala.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.LotteryRepository
import com.impala.campala.testutil.clearFirestore
import com.impala.campala.testutil.clickContentDescription
import com.impala.campala.testutil.clickText
import com.impala.campala.testutil.launchApp
import com.impala.campala.testutil.openLottery
import com.impala.campala.testutil.seedCampsite
import com.impala.campala.testutil.seedLottery
import com.impala.campala.testutil.signInAsNewEntrant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
* UI test for joining an open lottery from the campsite details page
*
* It goes Search tab -> Details Page -> Join Lottery -> Search Tab -> Lotteries Tab
* then checks that the lottery shows up in the lottery tab, and that the user has been added to the lottery in firebase
*
* The data is seeded straight into the Firebase emulator before the app is rendered
* Each test signs in as a brand new anonymous user, so tests can't see each other's lotteries/reservations
*/
@RunWith(AndroidJUnit4::class)
class JoinLotteryFlowTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var uid: String

    private val campsite = CampsiteModel(
        name = "Sietch Tabr",
        location = "Arrakis",
        rules = "Don't spill your water"
    )

    @Before
    fun seed(): Unit = runBlocking {
        clearFirestore()
        uid = signInAsNewEntrant()
        campsite.ownerId = uid
        seedCampsite(campsite)
        seedLottery(openLottery(listOf(campsite.id)))
    }

    @Test
    fun entrantCanJoinLotteryTest() {
        compose.launchApp()

        compose.clickText("Search")
        compose.clickText("Sietch Tabr")
        compose.clickText("Join Lottery")
        compose.clickContentDescription("Back Icon")

        compose.clickText("Lotteries")
        compose.onNodeWithText("Sietch Tabr").assertExists()

        val saved = runBlocking { LotteryRepository.getLotteriesByEntrantId(uid) }
        assertEquals(1, saved.size)
        assertEquals(listOf(uid), saved[0].entrantIds)
    }
}