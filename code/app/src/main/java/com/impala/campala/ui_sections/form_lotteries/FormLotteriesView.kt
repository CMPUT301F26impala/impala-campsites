package com.impala.campala.ui_sections.form_lotteries

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.impala.campala.ViewObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.impala.campala.model.CampsiteModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.Date
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

/**
 * View for the create and edit version of the lottery form
 * The fields displayed will depend on whether a lottery was passed to the form.
 * The view stores the information needed to display the input boxes, campsite dropdown and tags.
 * It receives changes from the model and sends user inputs to the controller.
 *
 * @param controller the FormLotteriesController used to send user actions to the model
 */
class FormLotteriesView(
    val controller: FormLotteriesController
): ViewObserver<FormLotteriesModel> {
    // variables max participants, waitlist, winners,
    // for the list of campsites since we have multiple campsite
    private var selectedCampsiteIds by mutableStateOf(listOf<String>())
    private var maximumParticipants by mutableStateOf("")
    private var waitlistSize by mutableStateOf("")
    private var numberOfWinners by mutableStateOf("")

    // also need variables for date register available and booking
    private var registrationPeriodStartDate by mutableStateOf<Timestamp?>(null)
    private var registrationPeriodEndDate by mutableStateOf<Timestamp?>(null)
    private var bookingPeriodStartDate by mutableStateOf<Timestamp?>(null)
    private var bookingPeriodEndDate by mutableStateOf<Timestamp?>(null)
    private var availableDatesStart by mutableStateOf<Timestamp?>(null)
    private var availableDatesEnd by mutableStateOf<Timestamp?>(null)

    // when true it means lottery was passed, so edit mode
    // if not then go for create lottery version
    private var isEditing by mutableStateOf(false)
    private var wasLotterySavedSuccessfully by mutableStateOf(false)

    // dropdown data for the campsites
    private var dropdownDataCampsites by mutableStateOf(listOf<CampsiteModel>())
    private var dropdownDroppedFlag by mutableStateOf(false) // checks if the dropdown dropped

    /**
     * Used to copy the updated lottery information into the form's fields.
     *
     * @param model the FormLotteriesModel containing the updated information
     */
    override fun update(model: FormLotteriesModel) {
        // variables max participants, waitlist, winners,
        // for the list of campsites since we have multiple campsite
        selectedCampsiteIds = model.campsiteIds.toList()
        maximumParticipants = model.maximumParticipants
        waitlistSize = model.waitlistSize
        numberOfWinners = model.numberOfWinners

        // also need variables for date register available and booking
        registrationPeriodStartDate = model.registrationPeriodStartDate
        registrationPeriodEndDate = model.registrationPeriodEndDate
        availableDatesStart = model.availableDatesStart
        availableDatesEnd = model.availableDatesEnd
        bookingPeriodStartDate = model.bookingPeriodStartDate
        bookingPeriodEndDate = model.bookingPeriodEndDate

        // when true it means lottery was passed, so edit mode
        // if not then go for create lottery versino
        isEditing = (model.lottery != null)
        wasLotterySavedSuccessfully = model.wasLotterySavedSuccessfully

        // dropdown data for the available campsites
        dropdownDataCampsites = model.campsiteDropdownData
    }
    init {
        update(controller.model)
    }


    /**
     * The main render function to output the lottery form screen.
     *
     * @param navController the root NavController, used to go back once the lottery is saved
     * @param lotteryId the id of the lottery to edit, or null to create a new one
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun Render(navController: NavController, lotteryId: String?) {
        LaunchedEffect(lotteryId) {
            controller.onOpen(lotteryId)
        }

        LaunchedEffect(wasLotterySavedSuccessfully) {
            if (wasLotterySavedSuccessfully == true) {
                controller.onSaveNavigationBecomesFalse()
                navController.popBackStack()
            }
            // get data for campsites dropdown every time the screen is open so its updated live,
            // in case of lets say the user goes back and creates a new campsite before saving
            controller.dropdownCampsitesData()
        }

        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // heading
                if (isEditing == true) {
                    item {
                        Text(
                            text = "Edit Lottery",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "Create Lottery",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } // end of heading

                // dropdown for selecting campsites
                // new dropdown style since outlinedTextFIeld
                // is not clickable in the normal way wen trying to use it to make a dropdown
                item {
                    ExposedDropdownMenuBox(
                        expanded = dropdownDroppedFlag,
                        onExpandedChange = { dropdownDroppedFlag = it }
                    ) {
                        // need this specific outlinedTextFIeld or the style is weird
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Campsite") },
                            placeholder = { Text("Choose a location") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownDroppedFlag) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownDroppedFlag,
                            onDismissRequest = { dropdownDroppedFlag = false }
                        ) {
                            dropdownDataCampsites.forEach { campsite ->
                                DropdownMenuItem(
                                    text = { Text(campsite.name) },
                                    onClick = {
                                        controller.onAddCampsite(campsite.id)
                                        dropdownDroppedFlag = false
                                    }
                                )
                            }
                        }
                    }
                }


                // tag list for the location so user cn select multiple locations
                if (selectedCampsiteIds.size != 0) {
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // loop on every tag inside the tags list
                            selectedCampsiteIds.forEach { campsiteId ->
                                val campsite = dropdownDataCampsites.find { it.id == campsiteId }
                                // need to check null here or kotlin gives annoying error of not knowing if campsite is null
                                if (campsite != null) {
                                    InputChip(
                                        selected = true,
                                        onClick = { controller.onRemoveCampsite(campsiteId) },
                                        label = { Text(campsite.name) },
                                        trailingIcon = { Text("×") }
                                    )
                                }
                            }
                        } // end of flowrow
                    }
                }

                // maximum participant

                // maximum participants input
                item {
                    OutlinedTextField(
                        value = maximumParticipants,
                        onValueChange = {
                            // if the thing is anything but null, even space or empty or letter, dont work otherwise
                            // make separate isEmpty or user cant delete anything when they edit the lottery
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                controller.onMaximumParticipantsChange(it)
                            }
                        },
                        label = { Text("Maximum Participants") },
                        placeholder = { Text("Enter participants here") },
                        // number only keyboard so ideally they cant put anything else there
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // waitlist size REMEMBER SUPPORT TEXT
                item {
                    OutlinedTextField(
                        value = waitlistSize,
                        onValueChange = {
                            // make separate isEmpty or user cant delete anything when they edit the lottery
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                controller.onWaitlistSizeChange(it)
                            }
                        },
                        label = { Text("Waitlist Size") },
                        placeholder = { Text("Enter the waitlist size here") },
                        supportingText = { Text("Enter 0 to disable the waitlist for your listing") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // number of winners REMEMBER SUPPORT TEXT

                // number of winners input
                item {
                    OutlinedTextField(
                        value = numberOfWinners,
                        onValueChange = {
                            // make separate isEmpty or user cant delete anything
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                controller.onNumberOfWinnersChange(it)
                            }
                        },
                        label = { Text("Number of Winners") },
                        placeholder = { Text("Enter the number of winners here") },
                        supportingText = { Text("Winners will be drawn after the registration period ends") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // registration booked available: start end dates input side by side
                // registration
                item {
                    Column {
                        Text(text = "Registration Period", color = MaterialTheme.colorScheme.onBackground)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(39.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DateInput(
                                insideText = "Start Date",
                                chosenDate = registrationPeriodStartDate,
                                onDateChange = { controller.onRegistrationPeriodStartDateChange(it) }
                            )
                            DateInput(
                                insideText = "End Date",
                                chosenDate = registrationPeriodEndDate,
                                onDateChange = { controller.onRegistrationPeriodEndDateChange(it) }
                            )
                        }
                    }
                }

                // booking
                item {
                    Column {
                        Text(text = "Booking Period", color = MaterialTheme.colorScheme.onBackground)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(39.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DateInput(
                                insideText = "Start Date",
                                chosenDate = bookingPeriodStartDate,
                                onDateChange = { controller.onBookingPeriodStartDateChange(it) }
                            )
                            DateInput(
                                insideText = "End Date",
                                chosenDate = bookingPeriodEndDate,
                                onDateChange = { controller.onBookingPeriodEndDateChange(it) }
                            )
                        }
                    }
                }
                // available
                item {
                    Column {
                        Text(text = "Dates Available", color = MaterialTheme.colorScheme.onBackground)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(39.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DateInput(
                                insideText = "Start Date",
                                chosenDate = availableDatesStart,
                                onDateChange = { controller.onAvailableDatesStartChange(it) }
                            )
                            DateInput(
                                insideText = "End Date",
                                chosenDate = availableDatesEnd,
                                onDateChange = { controller.onAvailableDatesEndChange(it) }
                            )
                        }
                    }
                }

            } // personal refernece, end of lazy column

            // button for cancel and create/save
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { navController.popBackStack() }) { Text("Cancel") }
                Spacer(modifier = Modifier.width(24.dp))
                Button(onClick = { controller.onSaveClick() }) {
                    if (isEditing) {
                        Text("Save")
                    } else {
                        Text("Create")
                    }
                }
            }


        } // end of column
    } // end of render function


    /**
     * Used in render to create the 6 date input boxes.
     * Displays the selected date on a button, or the field name if no date was selected.
     * When pressed, opens a date picker where the user can enter a date.
     * The selected date is then passed back using onDateChange after pressing OK.
     *
     * @param insideText the text to display if no date has been selected
     * @param chosenDate the current date selected, or null if no date was selected
     * @param onDateChange the function called to update the date after the user selects one
     * @return Unit, but renders a date input button as a side effect
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DateInput(
        insideText: String,
        chosenDate: Timestamp?,
        onDateChange: (Timestamp) -> Unit
    ) {
        val currentDatePicker = rememberDatePickerState(
            initialSelectedDateMillis = chosenDate?.toDate()?.time,
            initialDisplayMode = DisplayMode.Input
        )
        var openPopupForDatePick by remember { mutableStateOf(false) }

        val formatOfDate = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        formatOfDate.timeZone = TimeZone.getTimeZone("UTC")

        val stirngOfDate = if (chosenDate == null) insideText
        else formatOfDate.format(chosenDate.toDate())

        OutlinedButton(
            onClick = { openPopupForDatePick = true }
        ) { Text(stirngOfDate) }

        if (openPopupForDatePick) {
            DatePickerDialog(
                onDismissRequest = { openPopupForDatePick = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openPopupForDatePick = false
                            val selectedMillis = currentDatePicker.selectedDateMillis
                            if (selectedMillis != null) { onDateChange(Timestamp(Date(selectedMillis))) }
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { openPopupForDatePick = false }
                    ) { Text("Cancel") }
                }
            ) {
                DatePicker(state = currentDatePicker)
            }
        }
    } // end of date input function
}
