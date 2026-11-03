package com.impala.campala.ui_sections.form_lotteries

import com.google.firebase.Timestamp
import com.impala.campala.Controller

class FormLotteriesController(
    val model: FormLotteriesModel
) : Controller {

    /*
    to do:
    have a function for saving lottery and changing the flag to false after saved to confirm something was saved
    have some basic functions that 1:1 reflect the model functions
    registration start end
    avaibale dates start end
    booking start end
    change for variables maxparticipi, waitlist, winners,
    for the list of campsites since we have multiple campsites ugh
    */
    // save and flag change
    fun onSaveClick() { model.saveLottery() }
    fun onSaveNavigationBecomesFalse() { model.changeSaveFlagFalse() }

    //registration and available and booking
    fun onRegistrationPeriodStartDateChange(newStartDate: Timestamp) { model.updateRegistrationPeriodStartDate(newStartDate) }
    fun onRegistrationPeriodEndDateChange(newEndDate: Timestamp) { model.updateRegistrationPeriodEndDate(newEndDate) }
    fun onBookingPeriodStartDateChange(newStartDate: Timestamp) { model.updateBookingPeriodStartDate(newStartDate) }
    fun onBookingPeriodEndDateChange(newEndDate: Timestamp) { model.updateBookingPeriodEndDate(newEndDate) }
    fun onAvailableDatesStartChange(newStartDate: Timestamp) { model.updateAvailableDatesStart(newStartDate) }
    fun onAvailableDatesEndChange(newEndDate: Timestamp) { model.updateAvailableDatesEnd(newEndDate) }

    // variables max participant, waitlist, winner, list
    fun onMaximumParticipantsChange(newMaximumParticipants: String) {
        model.updateMaximumParticipants(newMaximumParticipants)
    }
    fun onWaitlistSizeChange(newWaitlistSize: String) { model.updateWaitlistSize(newWaitlistSize) }
    fun onNumberOfWinnersChange(newNumberOfWinners: String) { model.updateNumberOfWinners(newNumberOfWinners) }
    fun onAddCampsite(newCampsiteId: String) { model.addCampsite(newCampsiteId) }
    fun onRemoveCampsite(campsiteId: String) { model.removeCampsite(campsiteId) }
}