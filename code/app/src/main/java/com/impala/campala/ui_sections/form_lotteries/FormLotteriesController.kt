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
    /**
     * Changes start of registration date
     *
     * @param newStartDate New registration start date
     */
    fun onRegistrationPeriodStartDateChange(newStartDate: Timestamp) { model.updateRegistrationPeriodStartDate(newStartDate) }

    /**
     * Changes end of registration date
     *
     * @param newEndDate New registration end date
     */
    fun onRegistrationPeriodEndDateChange(newEndDate: Timestamp) { model.updateRegistrationPeriodEndDate(newEndDate) }

    /**
     * Changes start of booking date
     *
     * @param newStartDate New booking start date
     */
    fun onBookingPeriodStartDateChange(newStartDate: Timestamp) { model.updateBookingPeriodStartDate(newStartDate) }

    /**
     * Changes end of booking date
     *
     * @param newEndDate New booking end date
     */
    fun onBookingPeriodEndDateChange(newEndDate: Timestamp) { model.updateBookingPeriodEndDate(newEndDate) }

    /**
     * Changes start of available dates for booking
     *
     * @param newStartDate New available start date
     */
    fun onAvailableDatesStartChange(newStartDate: Timestamp) { model.updateAvailableDatesStart(newStartDate) }

    /**
     * Changes end of available dates for booking
     *
     * @param newEndDate New available end date
     */
    fun onAvailableDatesEndChange(newEndDate: Timestamp) { model.updateAvailableDatesEnd(newEndDate) }

    /**
     * Changes maximum participants of the lottery
     *
     * @param newMaximumParticipants new limit of maximum participants
     */
    fun onMaximumParticipantsChange(newMaximumParticipants: String) {
        model.updateMaximumParticipants(newMaximumParticipants)
    }

    /**
     * Changes waitlist size of the lottery
     *
     * @param newWaitlistSize new waistlist size
     */
    fun onWaitlistSizeChange(newWaitlistSize: String) { model.updateWaitlistSize(newWaitlistSize) }

    /**
     * Changes number of winners in the lottery
     *
     * @param newNumberOfWinners Number of winners for the lottery
     */
    fun onNumberOfWinnersChange(newNumberOfWinners: String) { model.updateNumberOfWinners(newNumberOfWinners) }

    /**
     * Adds a campsite to chosen locations for a lottery
     *
     * @param newCampsiteId Campsite ID of the one being added
     */
    fun onAddCampsite(newCampsiteId: String) { model.addCampsite(newCampsiteId) }

    /**
     * Removes a campsite form chosen locations for a lottery
     *
     * @param campsiteId Campsite ID of the one being removed
     */
    fun onRemoveCampsite(campsiteId: String) { model.removeCampsite(campsiteId) }

    /**
     * Tells the model to fetch the available campsites for the dropdown in the form.
     */
    fun dropdownCampsitesData() {
        model.listAllAvailableCampsites()
    }
}