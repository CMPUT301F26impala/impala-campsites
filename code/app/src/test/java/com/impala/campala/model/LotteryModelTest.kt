package com.impala.campala.model

import com.google.firebase.Timestamp
import com.impala.campala.model.LotteryModel.JoinResult
import com.impala.campala.model.LotteryModel.ProspectiveJoinStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Unit tests just for the few non-firebase-related behaviours of LotteryModel
 * Firebase-related tests are in androidTest
 */
class LotteryModelTest {

    private fun daysFromToday(days: Long): Timestamp {
        return Timestamp(LocalDate.now().plusDays(days).atStartOfDay(ZoneOffset.UTC).toInstant())
    }

    private fun openLottery(
        entrantIds: List<String> = emptyList(),
        waitlistIds: List<String> = emptyList()
    ): LotteryModel {
        return LotteryModel(
            campsiteIds = mutableListOf("campsite"),
            lotteryStartTime = daysFromToday(-1),
            lotteryEndTime = daysFromToday(3),
            registrationStartTime = daysFromToday(3),
            registrationEndTime = daysFromToday(5),
            reservationStartDate = daysFromToday(10),
            reservationEndDate = daysFromToday(20),
            lotteryCapacity = 2,
            waitlistCapacity = 1,
            entrantIds = entrantIds.toMutableList(),
            waitlistIds = waitlistIds.toMutableList(),
            ownerId = "owner"
        )
    }

    // like an assert but for the validate() function
    private fun assertInvalid(expectedMessage: String, lottery: LotteryModel) {
        val error = assertThrows(IllegalArgumentException::class.java) { lottery.validate() }
        assertEquals(expectedMessage, error.message)
    }

    @Test
    fun validLotteryPassesValidation() {
        openLottery().validate()
    }

    @Test
    fun validationAllowsDatesThatLineUpExactly() {
        val day = daysFromToday(3)
        openLottery().copy(
            lotteryStartTime = day,
            lotteryEndTime = day,
            registrationStartTime = day,
            registrationEndTime = day,
            reservationStartDate = day,
            reservationEndDate = day
        ).validate()
    }

    @Test
    fun validationNeedsAnOwner() {
        assertInvalid("A lottery must have an owner", openLottery().copy(ownerId = " "))
    }

    @Test
    fun validationNeedsACampsite() {
        assertInvalid("A lottery must be linked to at least one campsite", openLottery().copy(campsiteIds = mutableListOf()))
    }

    @Test
    fun validationRejectsRegistrationEndingBeforeTheDraw() {
        assertInvalid("Registration can't end before winners are drawn", openLottery().copy(registrationEndTime = daysFromToday(2)))
    }

    @Test
    fun validationRejectsBackwardsReservationRange() {
        assertInvalid("Reservation range can't end before it starts", openLottery().copy(reservationEndDate = daysFromToday(9)))
    }

    @Test
    fun validationRejectsLotteryEndingBeforeItStarts() {
        assertInvalid("Lottery can't end before it starts.", openLottery().copy(lotteryStartTime = daysFromToday(4)))
    }

    @Test
    fun validationRejectsBookingEndingBeforeItStarts() {
        assertInvalid("Booking can't start before it ends", openLottery().copy(registrationStartTime = daysFromToday(6)))
    }

    @Test
    fun validationNeedsRoomForAtLeastOneEntrant() {
        assertInvalid("Lottery capacity must be at least 1", openLottery().copy(lotteryCapacity = 0, numberOfWinners = 0))
    }

    @Test
    fun validationRejectsNegativeWaitlist() {
        assertInvalid("Waitlist capacity can't be negative", openLottery().copy(waitlistCapacity = -1))
    }

    @Test
    fun validationRejectsTooManyEntrants() {
        assertInvalid("More entrants than the lottery capacity", openLottery(entrantIds = listOf("a", "b", "c")))
    }

    @Test
    fun validationRejectsTooManyOnWaitlist() {
        assertInvalid("More people on the waitlist than its capacity", openLottery(waitlistIds = listOf("a", "b")))
    }

    @Test
    fun validationRejectsMoreWinnersThanCapacity() {
        assertInvalid("More possible winners than the lottery's capacity", openLottery().copy(numberOfWinners = 3))
    }

    @Test
    fun canJoinLotteryWhenOpenWithRoom() {
        assertEquals(ProspectiveJoinStatus.CAN_JOIN_LOTTERY, openLottery().evaluateUserAction("harry"))
    }

    @Test
    fun canJoinWaitlistWhenLotteryIsFull() {
        val lottery = openLottery(entrantIds = listOf("ron", "hermione"))
        assertEquals(ProspectiveJoinStatus.CAN_JOIN_WAITLIST, lottery.evaluateUserAction("harry"))
    }

    @Test
    fun fullWhenLotteryAndWaitlistAreFull() {
        val lottery = openLottery(entrantIds = listOf("ron", "hermione"), waitlistIds = listOf("neville"))
        assertEquals(ProspectiveJoinStatus.FULL, lottery.evaluateUserAction("harry"))
    }

    @Test
    fun alreadyInLotteryOrWaitlist() {
        val lottery = openLottery(entrantIds = listOf("ron"), waitlistIds = listOf("neville"))
        assertEquals(ProspectiveJoinStatus.ALREADY_IN_LOTTERY, lottery.evaluateUserAction("ron"))
        assertEquals(ProspectiveJoinStatus.ALREADY_IN_WAITLIST, lottery.evaluateUserAction("neville"))
    }

    @Test
    fun peopleAlreadyInKeepTheirStatusAfterItCloses() {
        val closed = openLottery(entrantIds = listOf("ron"), waitlistIds = listOf("neville"))
            .copy(lotteryStartTime = daysFromToday(-5), lotteryEndTime = daysFromToday(-1))
        assertEquals(ProspectiveJoinStatus.ALREADY_IN_LOTTERY, closed.evaluateUserAction("ron"))
        assertEquals(ProspectiveJoinStatus.ALREADY_IN_WAITLIST, closed.evaluateUserAction("neville"))
    }

    @Test
    fun notOpenBeforeItStarts() {
        val upcoming = openLottery().copy(lotteryStartTime = daysFromToday(1))
        assertEquals(ProspectiveJoinStatus.NOT_OPEN, upcoming.evaluateUserAction("harry"))
    }

    @Test
    fun openOnTheDayItStarts() {
        val startsToday = openLottery().copy(lotteryStartTime = daysFromToday(0))
        assertEquals(ProspectiveJoinStatus.CAN_JOIN_LOTTERY, startsToday.evaluateUserAction("harry"))
    }

    @Test
    fun closedOnTheDayItEnds() {
        val endsToday = openLottery().copy(lotteryStartTime = daysFromToday(-2), lotteryEndTime = daysFromToday(0))
        assertEquals(ProspectiveJoinStatus.NOT_OPEN, endsToday.evaluateUserAction("harry"))
    }

    @Test
    fun joiningFillsLotteryThenWaitlist() {
        val lottery = openLottery()

        assertEquals(JoinResult.JOINED_LOTTERY, lottery.addEntrant("harry"))
        assertEquals(JoinResult.JOINED_LOTTERY, lottery.addEntrant("ron"))
        assertEquals(JoinResult.JOINED_WAITLIST, lottery.addEntrant("hermione"))
        assertEquals(JoinResult.FULL, lottery.addEntrant("neville"))

        assertEquals(listOf("harry", "ron"), lottery.entrantIds)
        assertEquals(listOf("hermione"), lottery.waitlistIds)
    }

    @Test
    fun joiningTwiceDoesNotAddTwice() {
        val lottery = openLottery()
        lottery.addEntrant("harry")
        lottery.addEntrant("ron")
        lottery.addEntrant("hermione")

        assertEquals(JoinResult.ALREADY_JOINED, lottery.addEntrant("harry"))
        assertEquals(JoinResult.ALREADY_JOINED, lottery.addEntrant("hermione"))
        assertEquals(listOf("harry", "ron"), lottery.entrantIds)
        assertEquals(listOf("hermione"), lottery.waitlistIds)
    }

    @Test
    fun cantJoinWhenNotOpen() {
        val closed = openLottery().copy(lotteryStartTime = daysFromToday(-5), lotteryEndTime = daysFromToday(-1))

        assertEquals(JoinResult.NOT_OPEN, closed.addEntrant("harry"))
        assertTrue(closed.entrantIds.isEmpty())
        assertTrue(closed.waitlistIds.isEmpty())
    }

    @Test
    fun noWaitlistMeansFullOnceLotteryFills() {
        val lottery = openLottery(entrantIds = listOf("ron", "hermione")).copy(waitlistCapacity = 0)
        assertEquals(JoinResult.FULL, lottery.addEntrant("harry"))
    }

    @Test
    fun leavingLotteryPromotesEarliestOnWaitlist() {
        val lottery = openLottery(entrantIds = listOf("harry", "ron"), waitlistIds = listOf("hermione", "neville"))

        assertTrue(lottery.removeEntrant("harry"))
        assertEquals(listOf("ron", "hermione"), lottery.entrantIds)
        assertEquals(listOf("neville"), lottery.waitlistIds)
    }

    @Test
    fun leavingLotteryWithEmptyWaitlistJustRemoves() {
        val lottery = openLottery(entrantIds = listOf("harry", "ron"))

        assertTrue(lottery.removeEntrant("harry"))
        assertEquals(listOf("ron"), lottery.entrantIds)
        assertTrue(lottery.waitlistIds.isEmpty())
    }

    @Test
    fun leavingWaitlistDoesNotPromoteAnyone() {
        val lottery = openLottery(entrantIds = listOf("harry", "ron"), waitlistIds = listOf("hermione"))

        assertTrue(lottery.removeEntrant("hermione"))
        assertEquals(listOf("harry", "ron"), lottery.entrantIds)
        assertTrue(lottery.waitlistIds.isEmpty())
    }

    @Test
    fun removingSomeoneWhoIsNotInReturnsFalse() {
        val lottery = openLottery(entrantIds = listOf("harry"), waitlistIds = listOf("hermione"))

        assertFalse(lottery.removeEntrant("neville"))
        assertEquals(listOf("harry"), lottery.entrantIds)
        assertEquals(listOf("hermione"), lottery.waitlistIds)
    }
}
