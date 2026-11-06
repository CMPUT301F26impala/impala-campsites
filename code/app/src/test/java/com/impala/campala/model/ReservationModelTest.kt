package com.impala.campala.model

import com.google.firebase.Timestamp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Unit tests just for the few non-firebase-related behaviours of ReservationModel
 * Firebase-related tests are in androidTest
 */
class ReservationModelTest {

    private fun stayEnding(daysFromToday: Long): ReservationModel {
        val lastDay = LocalDate.now().plusDays(daysFromToday)
        return ReservationModel(
            startDate = Timestamp(lastDay.minusDays(2).atStartOfDay(ZoneOffset.UTC).toInstant()),
            endDate = Timestamp(lastDay.atStartOfDay(ZoneOffset.UTC).toInstant())
        )
    }

    // This is all just just ensuring the isPast logic works
    @Test
    fun stayThatEndedYesterdayIsPast() {
        assertTrue(stayEnding(-1).isPast)
    }

    @Test
    fun stayEndingTodayIsNotPastYet() {
        assertFalse(stayEnding(0).isPast)
    }

    @Test
    fun stayEndingTomorrowIsNotPast() {
        assertFalse(stayEnding(1).isPast)
    }
}
