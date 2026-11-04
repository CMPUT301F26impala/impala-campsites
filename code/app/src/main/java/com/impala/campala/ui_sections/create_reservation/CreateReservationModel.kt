package com.impala.campala.ui_sections.create_reservation

import com.google.firebase.Timestamp
import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.LotteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

enum class CURRENT_RESERVATION_STAGE {
    TERMS_ACCEPT,
    PICK_CAMPSITE_AND_DATES,
    CONFIRM_MODAL,
    DECLINE_MODAL,
    CONFIRMATION
}

/**
 * Model for the Create Reservation screen
 */
class CreateReservationModel(val scope: CoroutineScope, val lotteryRepo: LotteryRepository, val campsiteRepo: CampsiteRepository): ObservableModel<CreateReservationModel>() {
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

    fun loadLotteryDetails(lotteryId: String) {
        currentStage = CURRENT_RESERVATION_STAGE.TERMS_ACCEPT
        selectedCampsite = null
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
        }
    }

    private fun timestampToLocalDate(timestamp: Timestamp): LocalDate {
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
