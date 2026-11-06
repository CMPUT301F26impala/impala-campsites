package com.impala.campala.integration

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.impala.campala.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest

// No emulator setup needed here: CampalaApplication points Firestore and Auth at the local emulators for debug builds.
class UserTest {
    // Start signed out so initialize() makes a brand new user, instead of picking up whoever the last test left signed in
    @Before
    fun signOut() {
        Firebase.auth.signOut()
    }

    // This is a test for Test-Driven-Development
    // I had the isuse where, if you tried to sign in, and the firebase AUTH had you recorded as having signed in previous
    // But the db had been wiped so you had no user record
    // It would crash.
    // So I wrote this test and fixed that problem
    @Test
    fun signedInUserWithNoUserDocumentGetsOneMade(): Unit = runBlocking {
        val uid = Firebase.auth.signInAnonymously().await().user!!.uid

        val buddy = User()
        buddy.initialize()

        assertEquals(uid, buddy.getUID())
        assertFalse(buddy.getHasCreatedEntrantProfile())
        assertFalse(buddy.getHasCreatedCampsiteOwnerProfile())

        // I had some issues with this not being await()ed, so I asked Claude Opus 5.5 for some help
        // It built the following retry loop, that just continually tries until it works
        val doc = Firebase.firestore.collection("users").document(uid)
        var exists = false
        repeat(50) {
            if (!exists) {
                exists = doc.get().await().exists()
                if (!exists) delay(100)
            }
        }
        assertTrue("initialize() should have made the /users document", exists)
    }

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