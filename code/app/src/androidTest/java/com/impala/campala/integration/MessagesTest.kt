package com.impala.campala.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.impala.campala.model.MessagesRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessagesTest {
    val repo = MessagesRepository()

    @Before
    fun setUp(){
        val db = FirebaseFirestore.getInstance()

        // Tasks.await() blocks the thread synchronously until the query finishes
        val snapshot = Tasks.await(db.collection("messages").get())

        val batch = db.batch()
        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }

        // Commit the batch synchronously too
        Tasks.await(batch.commit())
    }

    @Test
    fun createAndLookUpThread(): Unit = runBlocking {
        val stored = repo.getOrCreateThreadByReservation("resoID")
        assertNotNull("Thread should exist after creating", stored)
        val sameAsStored = repo.getOrCreateThreadByReservation("resoID")
        assertEquals("Should return the one already created when called again",sameAsStored,stored)
    }

//    @Test
//    fun sendMessage():Unit = runBlocking {
//        val stored = repo.getOrCreateThreadByReservation("resoID")
//        assertNotNull("Thread should exist after creating", stored)
//
//        repo.sendMessage()
//    }
}