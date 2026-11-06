package com.impala.campala.ui_sections.form_lotteries

import com.impala.campala.ObservableModel
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import kotlinx.coroutines.CoroutineScope
import com.google.firebase.Timestamp
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteOwnerProfileRepository
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset


/**
 * Model for the create and edit version of Lottery form.
 *
 * Stores the information entered into the form, and notifies view about the changes.
 * Saves lotteries after creation and editing, as well as loading them to display values to the user.
 * Also gets the list of campsites to be displayed in the dropdown.
 *
 * @param repo LotteryRepository consisting of the lotteries
 * @param scope the CoroutineScope used for Firebase operations
 * @param lottery lottery whose data will be edited, passed as null in creation mode
 * @param ownerId the id of the campsite owner creating the lottery
 * @param campsiteRepo Repository used to get the campsites for the dropdown
 * @param campsiteOwnerProfileRepo Repository used to find which campsites the owner owns, so only those are in the dropdown
 *
 * @property campsiteIds Stores a list of campsite IDs corresponding to those currently selected for the lottery
 * @property maximumParticipants Stores the value currently in the field for the maximum number of lottery participants
 * @property waitlistSize Stores the value currently in the field for the size of the waiting list
 * @property numberOfWinners Stores the value currently in the field for the number of lottery winners
 * @property wasLotterySavedSuccessfully True if the lottery has been saved in the database
 * @property registrationPeriodStartDate Stores the value currently in the field for the first day of the registration period
 * @property registrationPeriodEndDate Stores the value currently in the field for the last day of the registration period
 * @property bookingPeriodStartDate Stores the value currently in the field for the first day of the booking period
 * @property bookingPeriodEndDate Stores the value currently in the field for the last day of the booking period
 * @property availableDatesStart Stores the value currently in the field for the first available reservation date
 * @property availableDatesEnd Stores the value currently in the field for the last available reservation date
 */
class FormLotteriesModel(
    val repo: LotteryRepository,
    val scope: CoroutineScope,
    var lottery: LotteryModel? = null,
    val ownerId: String,
    val campsiteRepo: CampsiteRepository,
    val campsiteOwnerProfileRepo: CampsiteOwnerProfileRepository,
): ObservableModel<FormLotteriesModel>() {

    var campsiteIds = if (lottery == null) mutableListOf<String>() else lottery!!.campsiteIds.toMutableList()

    var maximumParticipants = if (lottery == null) "" else lottery!!.lotteryCapacity.toString()

    var waitlistSize = if (lottery == null) "" else lottery!!.waitlistCapacity.toString()

    var numberOfWinners = if (lottery == null) "1" else lottery!!.numberOfWinners.toString()

    var wasLotterySavedSuccessfully = false

    var registrationPeriodStartDate: Timestamp? = lottery?.lotteryStartTime
    var registrationPeriodEndDate: Timestamp? = lottery?.lotteryEndTime
    var bookingPeriodStartDate: Timestamp? = lottery?.registrationStartTime
    var bookingPeriodEndDate: Timestamp? = lottery?.registrationEndTime
    var availableDatesStart: Timestamp? = lottery?.reservationStartDate
    var availableDatesEnd: Timestamp? = lottery?.reservationEndDate

    /**
     * Prepares the model for creating a new lottery by clearing the form's fields.
     * Sets number of winners back to default value (1) and resets the save flag.
     */
    fun initializeCreateMode() {
        lottery = null
        campsiteIds = mutableListOf<String>()
        maximumParticipants = ""
        waitlistSize = ""
        numberOfWinners = "1"
        wasLotterySavedSuccessfully = false
        registrationPeriodStartDate = lottery?.lotteryStartTime
        registrationPeriodEndDate = lottery?.lotteryEndTime
        bookingPeriodStartDate = lottery?.registrationStartTime
        bookingPeriodEndDate = lottery?.registrationEndTime
        availableDatesStart = lottery?.reservationStartDate
        availableDatesEnd = lottery?.reservationEndDate
        this.notifyObservers(this)
    }

    /**
     * Prepares the model for editing an existing lottery by getting its information from the repo.
     * Updates the form's fields with the lottery data, or switches to creation mode if it was not found.
     * Notifies the view after updating the fields.
     *
     * @param lotteryId ID of the lottery being edited
     */
    fun initializeEditMode(lotteryId: String) {
        scope.launch {
            val currentLottery = repo.getLotteryById(lotteryId)
            if (currentLottery == null) {
                initializeCreateMode()
            } else {
                lottery = currentLottery
                campsiteIds = lottery?.campsiteIds!!.toMutableList()
                maximumParticipants = lottery?.lotteryCapacity.toString()
                waitlistSize = lottery?.waitlistCapacity.toString()
                numberOfWinners = lottery?.numberOfWinners.toString()
                wasLotterySavedSuccessfully = false
                registrationPeriodStartDate = lottery?.lotteryStartTime
                registrationPeriodEndDate = lottery?.lotteryEndTime
                bookingPeriodStartDate = lottery?.registrationStartTime
                bookingPeriodEndDate = lottery?.registrationEndTime
                availableDatesStart = lottery?.reservationStartDate
                availableDatesEnd = lottery?.reservationEndDate
                notifyObservers(this@FormLotteriesModel)
                listAllAvailableCampsites()
            }
        }

    }

    var campsiteDropdownData = listOf<CampsiteModel>()
    /**
     * Gets the list of campsites from the repo to display in the dropdown.
     * Only the owner's own campsites that ARENT in another lottery are listed.
     * A lottery counts as finished once its booking period end date has passed, since winners can reserve until then
     * Notifies the view after getting the list.
     */
    fun listAllAvailableCampsites() {
        // Remembered so that if a different lottery gets opened while this is loading, this result is thrown away instead of shown
        val editingLotteryId = lottery?.id
        scope.launch{
            val ownedCampsiteIds = campsiteOwnerProfileRepo.getCampsiteOwnerProfileById(ownerId)?.ownedCampsites ?: emptyList()

            val today = LocalDate.now()
            val takenCampsiteIds = repo.getLotteries()
                .filter { it.id != editingLotteryId } // The lottery being edited can keep its own campsites
                .filter { !today.isAfter(it.registrationEndTime.toInstant().atZone(ZoneOffset.UTC).toLocalDate()) }
                .flatMap { it.campsiteIds }
                .toSet()

            val availableCampsites = campsiteRepo.getFilteredCampsites()
                .filter { it.id in ownedCampsiteIds && it.id !in takenCampsiteIds }

            if (lottery?.id != editingLotteryId) return@launch
            campsiteDropdownData = availableCampsites
            notifyObservers(this@FormLotteriesModel)
        }
    }

    /**
     * Adds a campsite to the list of selected campsites if it isn't already there.
     * Notifies the view so the selected campsite can be displayed as a tag.
     *
     * @param campsiteId the id of the campsite being added
     */
    fun addCampsite(campsiteId: String) {
        // check for valid campsite or its problematic error
        if (!campsiteIds.contains(campsiteId)) {
            campsiteIds.add(campsiteId)
            notifyObservers(this)
        }
    }

    /**
     * Removes a campsite from the list of selected campsites.
     * Notifies the view to update the tags.
     *
     * @param campsiteId the id of the campsite to remove
     */
    fun removeCampsite(campsiteId: String) {
        campsiteIds.remove(campsiteId)
        notifyObservers(this)
    }

    /**
     * Updates the start date for when the campsite is available
     *
     * @param newStartDate the new starting date selected
     */
    fun updateAvailableDatesStart(newStartDate: Timestamp) {
        availableDatesStart = newStartDate
        notifyObservers(this)
    }

    /**
     * Updates the end date for when the campsite is available
     *
     * @param newEndDate the new ending date selected
     */
    fun updateAvailableDatesEnd(newEndDate: Timestamp) {
        availableDatesEnd = newEndDate
        notifyObservers(this)
    }

    /**
     * Updates the starting date of the lottery registration period
     *
     * @param newStartDate the new date when users can start joining the lottery
     */
    fun updateRegistrationPeriodStartDate(newStartDate: Timestamp) {
        registrationPeriodStartDate = newStartDate
        notifyObservers(this)
    }

    /**
     * Updates the ending date of the lottery registration period
     *
     * @param newEndDate the new date when the lottery registration ends
     */
    fun updateRegistrationPeriodEndDate(newEndDate: Timestamp) {
        registrationPeriodEndDate = newEndDate
        notifyObservers(this)
    }

    /**
     * Updates the starting date of the booking period
     *
     * @param newStartDate the new date when winners can start booking
     */
    fun updateBookingPeriodStartDate(newStartDate: Timestamp) {
        bookingPeriodStartDate = newStartDate
        notifyObservers(this)
    }

    /**
     * Updates the ending date of the booking period
     *
     * @param newEndDate the new date when winners can no longer book
     */
    fun updateBookingPeriodEndDate(newEndDate: Timestamp) {
        bookingPeriodEndDate = newEndDate
        notifyObservers(this)
    }

    /**
     * Updates the maximum number of participants allowed in the lottery
     *
     * @param newParticipants string data for maximum participants in the form
     */
    fun updateMaximumParticipants(newParticipants: String) {
        maximumParticipants = newParticipants
        notifyObservers(this)
    }

    /**
     * Updates the maximum number of people allowed on the waitlist
     *
     * @param newWaitlistSize string data for the waitlist size
     */
    fun updateWaitlistSize(newWaitlistSize: String) {
        waitlistSize = newWaitlistSize
        notifyObservers(this)
    }

    /**
     * Updates the number of winners to be drawn from the lottery
     *
     * @param newNumberOfWinners string data for the number of winners
     */
    fun updateNumberOfWinners(newNumberOfWinners: String) {
        numberOfWinners = newNumberOfWinners
        notifyObservers(this)
    }

    /**
     * Creates or updates a lottery using the form's values.
     * Converts the string inputs to integers, and checks if all the dates have been selected.
     * If a date is missing, the function returns without saving anything.
     * Saves the lottery to Firebase and notifies the view after it has been saved.
     */
    fun saveLottery() {
        val participants = if (maximumParticipants == "") 0 else maximumParticipants.toInt()
        val waitlistCapacity = if (waitlistSize == "") 0 else waitlistSize.toInt()
        val winnersCount = if (numberOfWinners == "") 1 else numberOfWinners.toInt()
        // use later for error messages in part 4 maybe like
        // please choose a registration date or please do blah blah
        if (registrationPeriodStartDate == null) return
        if (registrationPeriodEndDate == null) return
        if (bookingPeriodStartDate == null) return
        if (bookingPeriodEndDate == null) return
        if (availableDatesStart == null) return
        if (availableDatesEnd == null) return
        val updatedLottery: LotteryModel
        if (lottery == null) {
            updatedLottery = LotteryModel(
                campsiteIds = campsiteIds.toMutableList(),
                lotteryCapacity = participants,
                waitlistCapacity = waitlistCapacity,
                numberOfWinners = winnersCount,
                // checked for being null earlier so this forced ignore CANNOT give an error (i think)
                lotteryStartTime = registrationPeriodStartDate!!,
                lotteryEndTime = registrationPeriodEndDate!!,
                registrationStartTime = bookingPeriodStartDate!!,
                registrationEndTime = bookingPeriodEndDate!!,
                reservationStartDate = availableDatesStart!!,
                reservationEndDate = availableDatesEnd!!,
                ownerId = ownerId
            )
        } else {
            updatedLottery = lottery!!.copy(
                campsiteIds = campsiteIds.toMutableList(),
                lotteryCapacity = participants,
                waitlistCapacity = waitlistCapacity,
                numberOfWinners = winnersCount,
                lotteryStartTime = registrationPeriodStartDate!!,
                lotteryEndTime = registrationPeriodEndDate!!,
                registrationStartTime = bookingPeriodStartDate!!,
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
    } // personal reference, end of save function

    /**
     * When a lottery has been saved, wasLotterySavedSuccessfully becomes true for navigation.
     * This function changes that flag back to false after it has been used once,
     * so the form can be opened again without navigating back automatically.
     */
    fun changeSaveFlagFalse() {
        // use later to change flag to false since the user can keep
        // using the form after one submission and it needs to be changed for second try
        wasLotterySavedSuccessfully = false
        notifyObservers(this)
    }

}

