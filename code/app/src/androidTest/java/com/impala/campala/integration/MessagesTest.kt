package com.impala.campala.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.impala.campala.model.MessagesRepository
import com.impala.campala.model.ReservationModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessagesTest {
    @Before
    fun setUp(){
        val db = FirebaseFirestore.getInstance()

        // Tasks.await() blocks the thread synchronously until the query finishes
        var snapshot = Tasks.await(db.collection("messages").get())

        var batch = db.batch()
        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }

        // Commit the batch synchronously too
        Tasks.await(batch.commit())

        snapshot = Tasks.await(db.collection("reservations").get())

        batch = db.batch()
        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }

        // Commit the batch synchronously too
        Tasks.await(batch.commit())
    }

    @Test
    fun createAndLookUpThread(): Unit = runBlocking {
        val reso = ReservationModel(ownerId = "Own1", entrantId = "enter1")
        reso.save()
        val stored = MessagesRepository.getOrCreateThreadByReservation(reso.id)
        assertNotNull("Thread should exist after creating", stored)
        val sameAsStored = MessagesRepository.getOrCreateThreadByReservation(reso.id)
        assertEquals("Should return the one already created when called again",sameAsStored,stored)
    }

    @Test
    fun sendMessage():Unit = runBlocking {
        val reso = ReservationModel(ownerId = "Own2", entrantId = "enter2")
        reso.save()
        val stored = MessagesRepository.getOrCreateThreadByReservation(reso.id)
        assertNotNull("Thread should exist after creating", stored)

        MessagesRepository.sendMessage("Hello tenet, welcome to the lodge",true,stored)
        assertEquals("Hello tenet, welcome to the lodge",stored.messageThread[0].body)
        assert(stored.messageThread[0].fromOwner)
    }

    @Test
    fun sendMessageReload():Unit = runBlocking {
        val reso = ReservationModel(ownerId = "Own2", entrantId = "enter2")
        reso.save()
        val stored = MessagesRepository.getOrCreateThreadByReservation(reso.id)
        assertNotNull("Thread should exist after creating", stored)

        MessagesRepository.sendMessage("Hello tenet, welcome to the lodge",true,stored)
        assertEquals("Hello tenet, welcome to the lodge",stored.messageThread[0].body)
        val storedMessaged = MessagesRepository.getOrCreateThreadByReservation(reso.id)
        assertEquals("Hello tenet, welcome to the lodge",storedMessaged.messageThread[0].body)
    }

    @Test
    fun gettingThreads():Unit = runBlocking {
        assertEquals(0, MessagesRepository.getAllThreads().size)
        val reso1 = ReservationModel(ownerId = "Own1", entrantId = "enter1")
        reso1.save()
        val reso2 = ReservationModel(ownerId = "Own2", entrantId = "enter2")
        reso2.save()
        val reso3 = ReservationModel(ownerId = "Own2", entrantId = "enter3")
        reso3.save()
        MessagesRepository.getOrCreateThreadByReservation(reso1.id).save()
        MessagesRepository.getOrCreateThreadByReservation(reso2.id).save()
        MessagesRepository.getOrCreateThreadByReservation(reso3.id).save()
        assertEquals(3, MessagesRepository.getAllThreads().size)
        assertEquals(2, MessagesRepository.getThreadByOwner("Own2").size)
        assertEquals(1, MessagesRepository.getThreadByOwner("Own1").size)
        assertEquals(1, MessagesRepository.getThreadByEntrant("enter1").size)
    }

    @Test
    fun sendMessageDelete():Unit = runBlocking {
        val reso = ReservationModel(ownerId = "Own2", entrantId = "enter2")
        reso.save()
        val stored = MessagesRepository.getOrCreateThreadByReservation(reso.id)
        assertNotNull("Thread should exist after creating", stored)

        MessagesRepository.sendMessage("Hello tenet, welcome to the lodge",true,stored)
        assertEquals("Hello tenet, welcome to the lodge",stored.messageThread[0].body)
        var storedMessaged = MessagesRepository.getOrCreateThreadByReservation(reso.id)
        assertEquals("Hello tenet, welcome to the lodge",storedMessaged.messageThread[0].body)
        MessagesRepository.deleteThread(reso.id)
        storedMessaged = MessagesRepository.getOrCreateThreadByReservation(reso.id)
        assertEquals(0,storedMessaged.messageThread.size)
    }
}