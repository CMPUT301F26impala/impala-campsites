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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.impala.campala.model.CampsiteModel

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


                // maximum participant

                // waitlist size REMEMBER SUPPORT TEXT

                // number of winners REMEMBER SUPPORT TEXT

                // registration start and dates available

                // button for cancel create



            } // personal refernece, end of lazy column
        } // end of column
    }

}
