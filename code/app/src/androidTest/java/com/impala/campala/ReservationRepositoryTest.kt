package com.impala.campala

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.ReservationModel
import com.impala.campala.model.ReservationRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class ReservationRepositoryTest {
    val campsite = CampsiteModel(
        "azkaban",
        "prison",
        "bad place",
        69420,
        "TBD",
        "",
        mutableListOf("slytherins welcome")
    )
    val start: LocalDate = LocalDate.of(2026, 10, 27)
    val end: LocalDate = LocalDate.of(2026, 10, 30)

    val reservation = ReservationModel(
        campsite.id,
        "owner.id",
        "entrant.id",
        Timestamp(start.atStartOfDay(ZoneId.systemDefault()).toInstant()),
        Timestamp(end.atStartOfDay(ZoneId.systemDefault()).toInstant()),
    )

    val repo = ReservationRepository()

    @Before
    fun setUp() {
        val db = FirebaseFirestore.getInstance()

        // Tasks.await() blocks the thread synchronously until the query finishes
        val snapshot = Tasks.await(db.collection("reservations").get())

        val batch = db.batch()
        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }

        // Commit the batch synchronously too
        Tasks.await(batch.commit())
    }

    @Test
    fun createReservation(): Unit = runBlocking {
        repo.saveReservation(reservation)

        val stored = repo.getReservationById(reservation.id)
        assertNotNull("Reservation should exist after saving", stored)
        assertEquals(reservation,stored)
    }

    @Test
    fun getReservationsForEntrantsAndOwners(): Unit = runBlocking {
        var stored = repo.getReservationsByUser("Does_not_exist")
        assertEquals(0,stored.size)
        stored = repo.getReservationsByOwner("Does_not_exist")
        assertEquals(0,stored.size)

        val other = reservation.copy(id = "other", entrantId = "other-entrant", ownerId = "other-owner")
        val other1 = reservation.copy(id = "other-1", ownerId = "other-owner")
        // @Before is run on every test, so we have to set up our states again
        repo.saveReservation(reservation)
        repo.saveReservation(other)
        repo.saveReservation(other1)
        stored = repo.getReservationsByOwner(reservation.ownerId)
        assertEquals(1,stored.size) // original
        stored = repo.getReservationsByOwner("other-owner")
        assertEquals(2,stored.size) // other and other1
        stored = repo.getReservationsByUser(reservation.entrantId)
        assertEquals(2,stored.size) // original and other1
        stored = repo.getReservationsByUser("other-entrant")
        assertEquals(1,stored.size) // other
    }

    @Test
    fun getReservationByIdMissing(): Unit = runBlocking {
        assertNull("Getting a reservation that was never saved should return null", repo.getReservationById("does-not-exist"))
    }

    @Test
    fun getReservationByIdPicksCorrectReservation(): Unit = runBlocking {
        val other = reservation.copy(id = "other-res")
        repo.saveReservation(reservation)
        repo.saveReservation(other)

        assertEquals(reservation,repo.getReservationById(reservation.id))
        assertEquals(other,repo.getReservationById(other.id))
    }

    @Test
    fun updateReservation_savingWithSameIdOverwitesInsteadOfCreatingDuplicate(): Unit = runBlocking {
        repo.saveReservation(reservation)

        val updated = reservation.copy(campsiteId = "newer", ownerId = "BetterOwner")
        repo.saveReservation(updated)

        assertEquals(updated,repo.getReservationById(reservation.id))
        assertEquals(1,repo.getReservationsByUser(reservation.entrantId).size)
    }

    @Test
    fun deleteReservation(): Unit = runBlocking {
        repo.saveReservation(reservation)
        assertNotNull("Reservation should exist before deleting",repo.getReservationById(reservation.id))

        repo.deleteReservation(reservation)
        assertNull("Reservation should not exist after deleting",repo.getReservationById(reservation.id))
    }
}