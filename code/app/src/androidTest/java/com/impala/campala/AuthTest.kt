package com.impala.campala

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AuthTest {
    @Before
    fun resetAuth() {
        Firebase.auth.signOut()
    }

    @Test
    fun firebaseAuthTest() {
        val result = Tasks.await(Firebase.auth.signInAnonymously(), 10, TimeUnit.SECONDS)
        assertNotNull("User should be signed in", result.user)
    }
}
