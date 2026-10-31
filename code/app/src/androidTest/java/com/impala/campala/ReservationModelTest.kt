package com.impala.campala

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.ReservationModel
import com.impala.campala.model.ReservationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ReservationModelTest {
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
    fun firestoreEmulatorRoundTrip() {

        Tasks.await(reservation.save(), 10, TimeUnit.SECONDS)

        val storedModel = ReservationModel.load(
            Tasks.await(
                Firebase.firestore.collection("reservations").document(reservation.id).get()
            )
        )

        assertEquals(reservation, storedModel)
    }

    @Test
    fun updateIsPast(){
        val storedModel = ReservationModel.load(
            Tasks.await(
                Firebase.firestore.collection("reservations").document(reservation.id).get()
            )
        )
        assert(!storedModel.isPast) { "IsPast should be initialized to false" }
        repo.updateIsPast(reservation.id)
        assert(storedModel.isPast) { "IsPast should be updated to true" }
    }
}