package com.impala.campala.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.impala.campala.model.CampsiteModel
import com.impala.campala.testutil.clearFirestore
import com.impala.campala.testutil.clickContentDescription
import com.impala.campala.testutil.clickText
import com.impala.campala.testutil.createOtherOwner
import com.impala.campala.testutil.launchApp
import com.impala.campala.testutil.seedCampsite
import com.impala.campala.testutil.signInAsAdmin
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI test for admin banning owners and campsites
 *
 * It goes Search tab -> Details Page -> Delete Campsite -> Delete -> Search Tab
 * then checks that the campsite no longer shows up in the search
 *
 * On a different campsite, it then goes Details Page -> Ban Campsite Owner -> Ban -> Search Tab
 * then checks that the campsite no longer shows up in search
 *
 * The data is seeded straight into the Firebase emulator before the app is rendered
 * Each test signs in as a brand new anonymous user, so tests can't see each other's lotteries/reservations
 */
class BanCampsitesOwnersFlowTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var uid: String
    private lateinit var ds9OwnerId: String
    private lateinit var chbOwnerId: String

    private val campsite1 = CampsiteModel(
        name = "Deep Space 9",
        location = "Orbiting Bajor",
        rules = "Don't let Odo catch you"
    )
    private val campsite2 = CampsiteModel(
        name = "Camp Half-Blood",
        location = "New York",
        rules = "Must be a demigod"
    )

    @Before
    fun seed(): Unit = runBlocking {
        clearFirestore()
        uid = signInAsAdmin()
        ds9OwnerId = createOtherOwner("Benjamin Sisko")
        chbOwnerId = createOtherOwner("Chiron")
        campsite1.ownerId = ds9OwnerId
        campsite2.ownerId = chbOwnerId
        seedCampsite(campsite1)
        seedCampsite(campsite2)
    }

    @Test
    fun adminCanDeleteCampsitesBanOwnersTest() {
        compose.launchApp()

        compose.clickText("Campsites")
        compose.clickText("Deep Space 9")
        compose.clickText("Delete Campsite")
        compose.clickText("Delete")
        compose.clickContentDescription("Back Icon")
        compose.onNodeWithText("Deep Space 9").assertDoesNotExist()

        compose.clickText("Camp Half-Blood")
        compose.clickText("Ban Campsite Owner")
        compose.clickText("Ban")
        compose.clickContentDescription("Back Icon")
        compose.onNodeWithText("Camp Half-Blood").assertDoesNotExist()
    }
}