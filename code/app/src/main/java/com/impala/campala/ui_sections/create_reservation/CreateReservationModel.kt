package com.impala.campala.ui_sections.create_reservation

import com.google.firebase.Timestamp
import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.LotteryRepository
import com.impala.campala.model.ReservationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

enum class CURRENT_RESERVATION_STAGE {
    TERMS_ACCEPT,
    PICK_CAMPSITE_AND_DATES,
    CONFIRMATION
}

/**
 * Model for the Create Reservation screen
 */
class CreateReservationModel(val scope: CoroutineScope, val lotteryRepo: LotteryRepository, val campsiteRepo: CampsiteRepository, val reservationRepo: ReservationRepository, val user: User): ObservableModel<CreateReservationModel>() {
    var currentStage: CURRENT_RESERVATION_STAGE = CURRENT_RESERVATION_STAGE.TERMS_ACCEPT

    var availableCampsites: List<CampsiteModel> = emptyList()

    var selectedCampsite: CampsiteModel? = null

    var dateRangeStart: LocalDate = LocalDate.now()

    // Should be inclusive, eg: this localDate is the last day that you can actually reserve, not one day after that
    var dateRangeEnd: LocalDate = LocalDate.now()

    var alreadyReservedDatesForSelectedCampsite: List<LocalDate> = emptyList()

    var selectedStartDate: LocalDate? = null

    var selectedEndDate: LocalDate? = null

    // As long as the start, end, and everything in between are between dateRangeStart and dateRangeEnd and also none of them are already reserved for that campsite
    // Should be recomputed any time selectedCampsite, dateRangeStart, dateRangeEnd, alreadyReservedDates, selectedStartDate, or selectedEndDate are changed
    var isDateRangeValid: Boolean = false

    // This should always be equal to (currentStage != TERMS_ACCEPT && selectedCampsite != null && isDateRangeValid)
    var canConfirm: Boolean = false

    private var isSaving: Boolean = false;

    fun loadLotteryDetails(lotteryId: String) {
        currentStage = CURRENT_RESERVATION_STAGE.TERMS_ACCEPT
        selectedCampsite = null
        alreadyReservedDatesForSelectedCampsite = emptyList()
        selectedEndDate = null
        selectedStartDate = null
        isDateRangeValid = false
        canConfirm = false
        this.notifyObservers(this)
        scope.launch {
            val lottery = lotteryRepo.getLotteryById(lotteryId)
            if (lottery == null) {
                // THIS SHOULD NEVER HAPPEN
                return@launch
            }
            dateRangeStart = timestampToLocalDate(lottery.reservationStartDate)
            dateRangeEnd = timestampToLocalDate(lottery.reservationEndDate)

            // I used Claude Opus 5.5 with the prompt "How can I do promise.all() in Kotlin"
            // I know how to write this kind of parralel async code in Javascript and so I described what I wanted and it helped me with this Kotlin
            availableCampsites = lottery.campsiteIds.map { campsiteId ->
                async { campsiteRepo.getCampsiteById(campsiteId) }
            }.awaitAll().filterNotNull()

            if (availableCampsites.size == 1) {
                // If there's only one campsite, we auto-select it and then check it's dates
                setSelectedCampsite(availableCampsites[0].id)
            }

            recalculateValidity()
            notifyObservers(this@CreateReservationModel)
        }
    }

    fun setSelectedCampsite(campsiteId: String) {
        val model = availableCampsites.find { campsite -> campsite.id == campsiteId }
        if (model == null) {
            // SHOULD NEVER HAPPEN
            return
        }
        selectedCampsite = model
        // Clear the old campsite's dates right away so they aren't shown for this one while loading
        alreadyReservedDatesForSelectedCampsite = emptyList()
        recalculateValidity()
        notifyObservers(this)

        scope.launch {
            val reservedDates = mutableSetOf<LocalDate>()
            for (reservation in reservationRepo.getReservationsByCampsite(campsiteId)) {
                // Every day of the reservation counts as reserved, including its end date
                // Only the days inside this lottery's range are kept, the rest are ignored so the array isn't like absurdly long
                var date = maxOf(timestampToLocalDate(reservation.startDate), dateRangeStart)
                val lastDate = minOf(timestampToLocalDate(reservation.endDate), dateRangeEnd)
                while (!date.isAfter(lastDate)) {
                    reservedDates.add(date)
                    date = date.plusDays(1)
                }
            }
            alreadyReservedDatesForSelectedCampsite = reservedDates.sorted()
            recalculateValidity()
            notifyObservers(this@CreateReservationModel)
        }
    }

    fun acceptTerms() {
        currentStage = CURRENT_RESERVATION_STAGE.PICK_CAMPSITE_AND_DATES
        recalculateValidity()
        this.notifyObservers(this)
    }

    /**
     * Sets the first day of the user's chosen stay
     *
     * @param date the start date, null if it's been cleared or isn't a full date yet
     */
    fun updateSelectedStartDate(date: LocalDate?) {
        selectedStartDate = date
        recalculateValidity()
        notifyObservers(this)
    }

    /**
     * Sets the last day of the user's chosen stay, this day is included in the stay
     *
     * @param date the end date, null if it's been cleared or isn't a full date yet
     */
    fun updateSelectedEndDate(date: LocalDate?) {
        selectedEndDate = date
        recalculateValidity()
        notifyObservers(this)
    }

    /**
     * Called AFTER campsites and dates have been selected, and does the work of actually creating the reservation entry
     */
    fun completeReservation() {
        if (!canConfirm || isSaving) return;
        isSaving = true
        // DO THINGS
        isSaving = false;
        currentStage = CURRENT_RESERVATION_STAGE.CONFIRMATION
    }

    /**
     * Can be called anytime, and will irreversibly decline the reservation such that this user can't reserve from this lottery any more
     */
    fun declineReservation() {
        if (isSaving) return;
        isSaving = true
        // DO THINGS
        isSaving = false;
    }

    /**
     * A helper function that recalculates isDateRangeValid and canConfirm whenever anything they depend on changes
     * It has to be called from anything that changes the dates or the campsite
     */
    private fun recalculateValidity() {
        val start = selectedStartDate
        val end = selectedEndDate
        // This block, in order does,
        // Ensure nothing is null
        // Ensure the range is positive
        // Ensure the range is inside the available dates
        // Ensure none of the dates in the range are taken
        isDateRangeValid = selectedCampsite != null &&
            start != null && end != null &&
            !end.isBefore(start) &&
            !start.isBefore(dateRangeStart) && !end.isAfter(dateRangeEnd) &&
            alreadyReservedDatesForSelectedCampsite.none { reserved -> !reserved.isBefore(start) && !reserved.isAfter(end) }

        canConfirm = currentStage != CURRENT_RESERVATION_STAGE.TERMS_ACCEPT && selectedCampsite != null && isDateRangeValid
    }

    private fun timestampToLocalDate(timestamp: Timestamp): LocalDate {
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
