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

class FormLotteriesView(
    val controller: FormLotteriesController
): ViewObserver<FormLotteriesModel> {
    // variables maxparticipi, waitlist, winners,
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
    // if not then go for create lottery versino
    private var isEditing by mutableStateOf(false)
    private var wasLotterySavedSuccessfully by mutableStateOf(false)

    // dropdown data for the campsites
    private var dropdownDataCampsites by mutableStateOf(listOf<CampsiteModel>())
    private var dropdownDroppedFlag by mutableStateOf(false) // checks if the dropdown dropped

    override fun update(model: FormLotteriesModel) {
        // variables maxparticipi, waitlist, winners,
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

    /*
    composable function components should include
    REMEMBER TO CHANGE THE FLAG TO FALSE OR PAGE WILL BE STUCK ON THE SAME ONE
    have a heading create/edit lottery in the if else block for the is edting flag
    */
    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun Render(navController: NavController) {
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
                item {
                    Box {
                        // box on top of the form where you actually pick the campsite you need
                        OutlinedButton(
                            onClick = { dropdownDroppedFlag = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Select Campsite") }

                        // actual menu that you see when you click the first box
                        DropdownMenu(
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
                        } // end of dropdown
                    }
                }
                // tag list for the location so user cn select multiple locations
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
                        Text("Registration Period")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DateInput(
                                fieldName = "Start Date",
                                selectedDate = registrationPeriodStartDate,
                                onDateChange = { controller.onRegistrationPeriodStartDateChange(it) }
                            )
                            DateInput(
                                fieldName = "End Date",
                                selectedDate = registrationPeriodEndDate,
                                onDateChange = { controller.onRegistrationPeriodEndDateChange(it) }
                            )
                        }
                    }
                }

                // booking
                item {
                    Column {
                        Text("Booking Period")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DateInput(
                                fieldName = "Start Date",
                                selectedDate = bookingPeriodStartDate,
                                onDateChange = { controller.onBookingPeriodStartDateChange(it) }
                            )
                            DateInput(
                                fieldName = "End Date",
                                selectedDate = bookingPeriodEndDate,
                                onDateChange = { controller.onBookingPeriodEndDateChange(it) }
                            )
                        }
                    }
                }
                // available
                item {
                    Column {
                        Text("Dates Available")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DateInput(
                                fieldName = "Start Date",
                                selectedDate = availableDatesStart,
                                onDateChange = { controller.onAvailableDatesStartChange(it) }
                            )
                            DateInput(
                                fieldName = "End Date",
                                selectedDate = availableDatesEnd,
                                onDateChange = { controller.onAvailableDatesEndChange(it) }
                            )
                        }
                    }
                }

                // button for cancel create
                // not here will be outside of lazy column



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

    // used in render to make the 6 different input boxes
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DateInput(
        fieldName: String,
        selectedDate: Timestamp?,
        onDateChange: (Timestamp) -> Unit
    ) {
        val currentDatePicker = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.toDate()?.time,
            initialDisplayMode = DisplayMode.Input
        )
        var openPopupForDatePick by remember { mutableStateOf(false) }

        val formatOfDate = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        formatOfDate.timeZone = TimeZone.getTimeZone("UTC")

        val stirngOfDate = if (selectedDate == null) "Select Date"
        else formatOfDate.format(selectedDate.toDate())

        OutlinedButton(
            onClick = { openPopupForDatePick = true }
        ) { Text("$fieldName: $stirngOfDate") }

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
