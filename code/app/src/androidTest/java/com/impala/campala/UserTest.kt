package com.impala.campala

import org.junit.Test
import kotlinx.coroutines.test.runTest

// No emulator setup needed here: CampalaApplication points Firestore and Auth at the local emulators for debug builds.
class UserTest {
    @Test
    fun newSignIn() = runTest {
        val buddy = User()
        buddy.initialize()
    }

    @Test
    fun changeValuesServerUpdate() = runTest {
        val buddy = User()
        buddy.initialize()

        val entrant = buddy.getIsEntrant()

        buddy.setIsEntrant(!entrant) // The saved value should be the opposite of what it currently is.
        buddy.updateUserProfile(buddy.getUID()) // Shouldn't usually do this !! assertion, but hey, this is a test. It's sorta the point.

        buddy.setIsEntrant(entrant) // Setting local back to normal, but it should be overwritten when updated.
        buddy.updateLocalDetails(buddy.getUID())

        assert(buddy.getIsEntrant() == !entrant)
    }
}