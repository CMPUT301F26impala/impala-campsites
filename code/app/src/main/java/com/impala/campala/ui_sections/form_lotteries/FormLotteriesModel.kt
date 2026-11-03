package com.impala.campala.ui_sections.form_lotteries

import com.impala.campala.ObservableModel
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import kotlinx.coroutines.CoroutineScope
import com.google.firebase.Timestamp
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.launch

class FormLotteriesModel(
    val repo: LotteryRepository,
    val scope: CoroutineScope,
    val lottery: LotteryModel? = null,
    val ownerId: String,
    val campsiteRepo: CampsiteRepository
): ObservableModel<FormLotteriesModel>() {

    var campsiteIds = if (lottery == null) mutableListOf<String>() else lottery.campsiteIds.toMutableList()

    var maximumParticipants = if (lottery == null) "" else lottery.lotteryCapacity.toString()

    var waitlistSize = if (lottery == null) "" else lottery.waitlistCapacity.toString()

    // lottery model didn't have a variable for this so empty string for now
    // text logan or maybe talk to him next morning
    var numberOfWinners = "1" // if (lottery == null) "1" else lottery?.numberOfWinners?.toString()

    var wasLotterySavedSuccessfully = false

    // start dates for registration and lottery will from time of creation
    var registrationPeriodStartDate: Timestamp? = null // lottery?.lotteryStartTime
    var registrationPeriodEndDate: Timestamp? = lottery?.lotteryEndTime
    var bookingPeriodStartDate: Timestamp? = null // lottery?.registrationStartTime
    var bookingPeriodEndDate: Timestamp? = lottery?.registrationEndTime
    var availableDatesStart: Timestamp? = lottery?.reservationStartDate
    var availableDatesEnd: Timestamp? = lottery?.reservationEndDate

    // write code for available campsites for dropdown data int he form
    var campsiteDropdownData = listOf<CampsiteModel>()
    fun listAllAvailableCampsites() {
        scope.launch{
            campsiteDropdownData = campsiteRepo.getFilteredCampsites()
            notifyObservers(this@FormLotteriesModel)
        }
    }

    fun addCampsite(campsiteId: String) {
        // check for valid campsite or its problematic error
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
    fun saveLottery() {
        val participants = if (maximumParticipants == "") 0 else maximumParticipants.toInt()
        val waitlistCapacity = if (waitlistSize == "") 0 else waitlistSize.toInt()
        val winnersCount = if (numberOfWinners == "") 1 else numberOfWinners.toInt()
        // use later for error messages in part 4 maybe like
        // please choose a registration date or please do blah blah
        if (registrationPeriodEndDate == null) return
        if (bookingPeriodEndDate == null) return
        if (availableDatesStart == null) return
        if (availableDatesEnd == null) return
        val updatedLottery: LotteryModel
        if (lottery == null) {
            updatedLottery = LotteryModel(
                campsiteIds = campsiteIds.toMutableList(),
                lotteryCapacity = participants,
                waitlistCapacity = waitlistCapacity,
                // checked for being null earlier so this forced ignore CANNOT give an error (i think)
                lotteryEndTime = registrationPeriodEndDate!!,
                registrationEndTime = bookingPeriodEndDate!!,
                reservationStartDate = availableDatesStart!!,
                reservationEndDate = availableDatesEnd!!,
                ownerId = ownerId
            )
        } else {
            updatedLottery = lottery.copy(
                campsiteIds = campsiteIds.toMutableList(),
                lotteryCapacity = participants,
                waitlistCapacity = waitlistCapacity,
                lotteryEndTime = registrationPeriodEndDate!!,
                registrationEndTime = bookingPeriodEndDate!!,
                reservationStartDate = availableDatesStart!!,
                reservationEndDate = availableDatesEnd!!,
            )
        }
        scope.launch {
            repo.saveLottery(updatedLottery)
            wasLotterySavedSuccessfully = true
            notifyObservers(this@FormLotteriesModel)
        }
    } // personal reference, end of save fucntion

    // use later to change flag to false since the user can keep
    // using the form after one submission and it needs to be changed for second try
    fun changeSaveFlagFalse() {
        wasLotterySavedSuccessfully = false
        notifyObservers(this)
    }

}

