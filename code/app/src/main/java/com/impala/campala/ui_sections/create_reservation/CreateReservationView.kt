package com.impala.campala.ui_sections.create_reservation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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
                    DrawDateFields()
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
     * Draws the Start Date and End Date fields
     */
    @Composable
    private fun DrawDateFields() {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateField("Start Date", selectedStartDate, Modifier.weight(1f)) { date ->
                controller.onStartDateChange(date)
            }
            DateField("End Date", selectedEndDate, Modifier.weight(1f)) { date ->
                controller.onEndDateChange(date)
            }
        }
    }

    /**
     * An OutlinedTextField that opens a date picker when pressed
     *
     * @param label the label shown on the field
     * @param date the currently picked date, null if none is picked
     * @param modifier android modifier
     * @param onDateChange called with the new date when one is picked, or null when it's cleared
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DateField(label: String, date: LocalDate?, modifier: Modifier, onDateChange: (LocalDate?) -> Unit) {
        var datePickerOpen by remember { mutableStateOf(false) }

        // I had problems with having the text input that opened up the dialog, because yknow a text input doesn't really want to open something else when pressed it wants to open the keyboard
        // So I asked Claude Opus 5.5 how to make an OutlinedTextField trigger other code when pressed, and it gave me the below LaunchedEffect block
        // the TextField itself is my code
        val interactionSource = remember { MutableInteractionSource() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                if (interaction is PressInteraction.Release) {
                    datePickerOpen = true
                }
            }
        }

        OutlinedTextField(
            value = date?.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            singleLine = true,
            interactionSource = interactionSource,
            trailingIcon = {
                if (date != null) {
                    IconButton(onClick = { onDateChange(null) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear $label")
                    }
                }
            },
            modifier = modifier
        )

        if (datePickerOpen) {
            // I Used Claude Opus 5.5 with the prompt "How can I wire up a DatePickerDialog in Kotlin such that it has a list of specific selectable dates"
            // It helped me create this rememberDatePickerState call
            // Aside from that, I still wrote the UI for this myself.
            val datePickerState = rememberDatePickerState(
                initialSelectedDate = date,
                initialDisplayedMonth = YearMonth.from(date ?: dateRangeStart),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        // The picker gives dates as milliseconds at midnight UTC
                        val day = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        return !day.isBefore(dateRangeStart) && !day.isAfter(dateRangeEnd) &&
                            day !in alreadyReservedDatesForSelectedCampsite
                    }

                    override fun isSelectableYear(year: Int): Boolean {
                        return year in dateRangeStart.year..dateRangeEnd.year
                    }
                }
            )
            DatePickerDialog(
                onDismissRequest = { datePickerOpen = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerOpen = false
                        onDateChange(datePickerState.getSelectedDate())
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { datePickerOpen = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
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
                var textColor = MaterialTheme.colorScheme.onPrimary
                if (date in alreadyReservedDatesForSelectedCampsite) {
                    // Reserved days stay red
                    backgroundColor = MaterialTheme.colorScheme.tertiary
                } else if (isDateSelected(date)) {
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    /**
     * Whether a day is part of the user's current selection, used to highlight it on the calendar
     *
     * With both dates picked, it's every day from the start to the end (in either order, so a backwards range still shows)
     * With only one picked, it's just that day
     *
     * @param date the day to check
     * @return true if the day should be highlighted
     */
    private fun isDateSelected(date: LocalDate): Boolean {
        val start = selectedStartDate
        val end = selectedEndDate
        if (start != null && end != null) {
            return !date.isBefore(minOf(start, end)) && !date.isAfter(maxOf(start, end))
        }
        return date == start || date == end
    }
}
