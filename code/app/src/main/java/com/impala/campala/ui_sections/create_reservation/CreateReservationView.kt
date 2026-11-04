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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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
    private var currentStage by mutableStateOf(CURRENT_RESERVATION_STAGE.TERMS_ACCEPT)
    private var availableCampsites by mutableStateOf<List<CampsiteModel>>(emptyList())
    private var selectedCampsite by mutableStateOf<CampsiteModel?>(null)
    private var dateRangeStart by mutableStateOf<LocalDate>(LocalDate.now())
    private var dateRangeEnd by mutableStateOf<LocalDate>(LocalDate.now())
    private var alreadyReservedDatesForSelectedCampsite by mutableStateOf<List<LocalDate>>(emptyList())
    private var selectedStartDate by mutableStateOf<LocalDate?>(null)
    private var selectedEndDate by mutableStateOf<LocalDate?>(null)
    private var isDateRangeValid by mutableStateOf(false)
    private var canConfirm by mutableStateOf(false)

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
    fun Render(lotteryId: String) {
        LaunchedEffect(lotteryId) {
            controller.onOpen(lotteryId)
        }

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
                    DrawTermsStage(Modifier.weight(1f))
                } else if (currentStage == CURRENT_RESERVATION_STAGE.PICK_CAMPSITE_AND_DATES) {
                    if (availableCampsites.size > 1) {
                        DrawCampsiteSelector()
                    }
                    DrawCalendar(dateRangeStart, dateRangeEnd)
                }
            }
        }
    }

    @Composable
    private fun DrawTermsStage(modifier: Modifier = Modifier) {
        Column(modifier = modifier.fillMaxWidth()) {
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
     * Draws a radio button for each campsite so the user can pick which one to reserve
     */
    @Composable
    private fun DrawCampsiteSelector() {
        // I googled some Radio button implementations for this
        Column(modifier = Modifier.selectableGroup()) {
            for (campsite in availableCampsites) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = campsite.id == selectedCampsite?.id,
                            onClick = { controller.onCampsiteSelect(campsite.id) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // onClick is null since the Row above already handles the tap
                    RadioButton(selected = campsite.id == selectedCampsite?.id, onClick = null)
                    Text(
                        campsite.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
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
                if (date in alreadyReservedDatesForSelectedCampsite) {
                    backgroundColor = MaterialTheme.colorScheme.tertiary
                }

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