package com.impala.campala

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest

class UserTest {
    @Before
    fun setUp() {
        val emulatorHost = "10.0.2.2" //May be subject to change depending on your machine. This will be visible when you have set up a firebase emulator locally
        val auth = FirebaseAuth.getInstance()
        auth.useEmulator(emulatorHost, 9099) // Connect Auth to local emulator
        Firebase.firestore.useEmulator(emulatorHost, 8080) // Connect Firestore to local emulator
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