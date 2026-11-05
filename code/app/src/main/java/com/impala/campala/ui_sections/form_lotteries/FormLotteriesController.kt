package com.impala.campala.ui_sections.form_lotteries

import com.google.firebase.Timestamp
import com.impala.campala.Controller


/**
 * Controller for the create and edit version lottery forms
 *
 * Receives actions from the view and calls the corresponding functions in the model.
 * Also decides whether the form should create a new lottery or load an existing one.
 *
 * @param model the FormLotteriesModel whose functions are called
 */
class FormLotteriesController(
    val model: FormLotteriesModel
) : Controller {

    /**
     * Used in a LaunchedEffect in the view, handles data initialization when the lottery form is opened.
     * Creates a new form if no lottery id was passed, or loads the existing lottery for editing.
     *
     * @param lotteryId the id of the lottery to edit, or null if it's 'add lottery' mode
     */
    fun onOpen(lotteryId: String?) {
        if (lotteryId == null) {
            model.initializeCreateMode()
        } else {
            model.initializeEditMode(lotteryId)
        }
    }
    /**
     * Called when the user presses create or save
     */
    fun onSaveClick() { model.saveLottery() }

    /**
     * Called after the lottery has been saved and tells the model to reset the flag for navigation.
     */
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

    /**
     * Tells the model to fetch the available campsites for the dropdown in the form.
     */
    fun dropdownCampsitesData() {
        model.listAllAvailableCampsites()
    }
}