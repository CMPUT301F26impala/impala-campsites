package com.impala.campala.ui_sections.form_lotteries

import com.impala.campala.ObservableModel
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import kotlinx.coroutines.CoroutineScope
import com.google.firebase.Timestamp

class FormLotteriesModel(
    val repo: LotteryRepository,
    val scope: CoroutineScope,
    val lottery: LotteryModel? = null
): ObservableModel<FormLotteriesModel>() {

    var campsiteIds = if (lottery == null) mutableListOf<String>() else lottery.campsiteIds.toMutableList()

    var maximumParticipants = if (lottery == null) "" else lottery.lotteryCapacity.toString()

    var waitlistSize = if (lottery == null) "" else lottery.waitlistCapacity.toString()

    // lottery model didn't have a variable for this so empty string for now
    var numberOfWinners = ""

    // start dates not present in the lottery model so left alone for now
    var registrationPeriodStartDate: Timestamp? = null
    var registrationPeriodEndDate: Timestamp? = lottery?.lotteryEndTime
    var bookingPeriodStartDate: Timestamp? = null
    var bookingPeriodEndDate: Timestamp? = lottery?.registrationEndTime
    var availableDatesStart: Timestamp? = lottery?.reservationStartDate
    var availableDatesEnd: Timestamp? = lottery?.reservationEndDate

    fun addCampsite(campsiteId: String) {
        if (!campsiteIds.contains(campsiteId)) {
            campsiteIds.add(campsiteId)
            notifyObservers(this)
        }
    }

    fun removeCampsite(campsiteId: String) {
        campsiteIds.remove(campsiteId)
        notifyObservers(this)
    }

    fun updateAvailableDatesStart(newStartDate: Timestamp) {
        availableDatesStart = newStartDate
        notifyObservers(this)
    }

    fun updateAvailableDatesEnd(newEndDate: Timestamp) {
        availableDatesEnd = newEndDate
        notifyObservers(this)
    }

    fun updateRegistrationPeriodStartDate(newStartDate: Timestamp) {
        registrationPeriodStartDate = newStartDate
        notifyObservers(this)
    }

    fun updateRegistrationPeriodEndDate(newEndDate: Timestamp) {
        registrationPeriodEndDate = newEndDate
        notifyObservers(this)
    }

    fun updateBookingPeriodStartDate(newStartDate: Timestamp) {
        bookingPeriodStartDate = newStartDate
        notifyObservers(this)
    }

    fun updateBookingPeriodEndDate(newEndDate: Timestamp) {
        bookingPeriodEndDate = newEndDate
        notifyObservers(this)
    }

    fun updateMaximumParticipants(newParticipants: String) {
        maximumParticipants = newParticipants
        notifyObservers(this)
    }

    fun updateWaitlistSize(newWaitlistSize: String) {
        waitlistSize = newWaitlistSize
        notifyObservers(this)
    }

    fun updateNumberOfWinners(newNumberOfWinners: String) {
        numberOfWinners = newNumberOfWinners
        notifyObservers(this)
    }


}

