package com.impala.campala.ui_sections.create_reservation

import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteModel
import java.time.LocalDate

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
class CreateReservationModel: ObservableModel<CreateReservationModel>() {
    var currentStage: CURRENT_RESERVATION_STAGE = CURRENT_RESERVATION_STAGE.TERMS_ACCEPT

    var availableCampsites: List<CampsiteModel> = emptyList()

    var selectedCampsite: CampsiteModel? = null

    var dateRangeStart: LocalDate = LocalDate.now()

    // Should be inclusive, eg: this localDate is the last day that you can actually reserve, not one day after that
    var dateRangeEnd: LocalDate = LocalDate.now()

    var alreadyReservedDatesForSelectedCampsite: List<LocalDate> = emptyList()

    var selectedStartDate: LocalDate = LocalDate.now()

    var selectedEndDate: LocalDate = LocalDate.now()

    // As long as the start, end, and everything in between are between dateRangeStart and dateRangeEnd and also none of them are already reserved for that campsite
    // Should be recomputed any time selectedCampsite, dateRangeStart, dateRangeEnd, alreadyReservedDates, selectedStartDate, or selectedEndDate are changed
    var isDateRangeValid: Boolean = false

    // This should always be equal to (currentStage != TERMS_ACCEPT && selectedCampsite != null && isDateRangeValid)
    var canConfirm: Boolean = false
}
