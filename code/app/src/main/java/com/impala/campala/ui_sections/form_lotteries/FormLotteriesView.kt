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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    fun Render(navController: NavController) {
        LaunchedEffect(wasLotterySavedSuccessfully) {
            if (wasLotterySavedSuccessfully == true) {
                controller.onSaveNavigationBecomesFalse()
                navController.popBackStack()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
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
                }
            }
        }
    }

}
