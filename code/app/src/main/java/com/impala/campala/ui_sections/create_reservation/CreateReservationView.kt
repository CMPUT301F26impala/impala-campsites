package com.impala.campala.ui_sections.create_reservation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.impala.campala.model.ReservationRepository
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import androidx.compose.ui.platform.LocalLocale
import com.impala.campala.ViewObserver
import com.impala.campala.model.CampsiteModel

class CreateReservationView(
    val controller: CreateReservationController,
    val reservationRepo: ReservationRepository,
    val scope: CoroutineScope
): ViewObserver<CreateReservationModel> {
    private var currentStage: CURRENT_RESERVATION_STAGE = CURRENT_RESERVATION_STAGE.TERMS_ACCEPT
    private var availableCampsites: List<CampsiteModel> = emptyList()
    private var selectedCampsite: CampsiteModel? = null
    private var dateRangeStart: LocalDate = LocalDate.now()
    private var dateRangeEnd: LocalDate = LocalDate.now()
    private var alreadyReservedDatesForSelectedCampsite: List<LocalDate> = emptyList()
    private var selectedStartDate: LocalDate = LocalDate.now()
    private var selectedEndDate: LocalDate = LocalDate.now()
    private var isDateRangeValid: Boolean = false
    private var canConfirm: Boolean = false

    override fun update(model: CreateReservationModel) {
        currentStage = model.currentStage
        availableCampsites = model.availableCampsites
        selectedCampsite = model.selectedCampsite
        dateRangeStart = model.dateRangeStart
        dateRangeEnd = model.dateRangeEnd
        alreadyReservedDatesForSelectedCampsite = model.alreadyReservedDatesForSelectedCampsite
        selectedStartDate = model.selectedStartDate
        selectedEndDate = model.selectedEndDate
        isDateRangeValid = model.isDateRangeValid
        canConfirm = model.canConfirm
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Composable
    fun Render(
    ) {
//        LaunchedEffect(Unit) {
//            for (reservation in reservationRepo.getReservationsByDateRange(
//                Timestamp(
//                    startDate.atStartOfDay(
//                        ZoneId.systemDefault()
//                    ).toInstant()
//                ), Timestamp(
//                    // the end date is inclusive (eg: if the lottery has reservation period from jan 1 to jan 20, you can book jan 20
//                    // So we have to fetch reservations between jan 1 and 00:00 on jan 21
//                    endDate.plusDays(1).atStartOfDay(
//                        ZoneId.systemDefault()
//                    ).toInstant()
//                )
//            )) {
//                reserved.add(reservation)
//            }
//        }
        Scaffold() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    "Reserve Campsite",
                    style = MaterialTheme.typography.headlineLarge
                )
                if (currentStage == CURRENT_RESERVATION_STAGE.TERMS_ACCEPT) {
                    DrawTermsStage()
                } else if (currentStage == CURRENT_RESERVATION_STAGE.PICK_CAMPSITE_AND_DATES) {
                    DrawCalendar(dateRangeStart, dateRangeEnd)
                }
            }
        }
    }

    @Composable
    private fun DrawTermsStage() {
        Column() {
            Text(
                "Campsite rules and policies",
                style = MaterialTheme.typography.bodyLarge
            )
            // TODO: Terms is not currently a property on anything.
            // If we want one global terms text for the whole app, we can hardcode it here (what I think we should do)
            // If we want a custom terms per campsite, we'd need this accept screen to be AFTER the campsite is already chosen
            // Probably as part of the confirmation modal
            Text("Lorem Ipsum Doner Sit Amet", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    controller.onAcceptTerms()
                }) {
                    Text("I accept", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

    }

    /**
     * Draws a calendar of every day from [startDate] up to and including [endDate]
     *
     * this function splits the range into months, and calls a subfunction to render each month
     *
     * @param startDate the first day to show
     * @param endDate the last day to show
     */
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Composable
    private fun DrawCalendar(startDate: LocalDate, endDate: LocalDate) {
        Column {
            var firstMonth = YearMonth.from(startDate)
            val lastMonth = YearMonth.from(endDate)
            // Effectively this is "for each month from first to last"
            while (!firstMonth.isAfter(lastMonth)) {
                Text(
                    firstMonth.month.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                // Only draw the part of this month that's inside the range
                DrawMonth(
                    maxOf(startDate, firstMonth.atDay(1)), // Either start of month or if the range starts halfway through the month, use that
                    minOf(endDate, firstMonth.atEndOfMonth()) // Same as above, for end or before
                )
                firstMonth = firstMonth.plusMonths(1)
            }
        }
    }

    /**
     * Draws one month's grid, from the start date up to the end date INCLUDING the end date
     * This is Minh's code, I've just extracted it to this function so that multi-month ranges can render multiple of these
     *
     * @param startDate the first day of this month to show
     * @param endDate the last day of this month to show
     */
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Composable
    private fun DrawMonth(startDate: LocalDate, endDate: LocalDate) {
        Grid(
            config = {
                repeat(7) {
                    column(1 / 7f)
                }
                gap(8.dp)
            }
        ) {
            Text("Mon")
            Text("Tue")
            Text("Wed")
            Text("Thu")
            Text("Fri")
            Text("Sat")
            Text("Sun")
            // theres probably a better way to insert blanks
            for (i in 1..(startDate.dayOfWeek.value - 1)) {
                Text("")
            }

            for (date in startDate.datesUntil(endDate.plusDays(1))) {
                var backgroundColor = MaterialTheme.colorScheme.primary

//                for (reservation in reserved) {
//                    if (Timestamp(
//                            date.atStartOfDay(
//                                ZoneId.systemDefault()
//                            ).toInstant()
//                        ) in reservation.startDate..reservation.endDate
//                    ) {
//                        backgroundColor = MaterialTheme.colorScheme.tertiary
//                        break
//                    }
//                }
                Text(
                    "${date.dayOfMonth}",
                    modifier = Modifier
                        .background(
                            color = backgroundColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}