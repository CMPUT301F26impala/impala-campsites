package com.impala.campala.ui_sections.admin_profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.impala.campala.ViewObserver
import com.impala.campala.model.AdminProfileModel

/**
 * View for the profiles tab of the admin.
 *
 * Displays the list of active user profiles fetched by the model.
 * Receives changes from the model and sends user actions to the controller.
 *
 * @param controller the AdminProfilesController used to send actions to the model
 */
class AdminProfilesView(
    val controller: AdminProfilesController
) : ViewObserver<AdminProfilesModel> {

    private var allActiveUserProfiles by mutableStateOf(listOf<AdminProfileModel>())
    private var isFetchingUserProfiles by mutableStateOf(false)
    private var wasProfileFetchUnsuccessful by mutableStateOf(false)

    /**
     * Moves profile details from the model into the ui
     *
     * @param model the model containing the required details
     */
    override fun update(model: AdminProfilesModel) {
        allActiveUserProfiles = model.allActiveUserProfiles
        isFetchingUserProfiles = model.isFetchingUserProfiles
        wasProfileFetchUnsuccessful = model.wasProfileFetchUnsuccessful
    }
    init { update(controller.model) }

    /**
     * The main render function to output the admin profiles screen.
     * Loads the profiles whenever the tab is opened.
     *
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    fun Render() {
        // first look at the ui
        LaunchedEffect(Unit) { controller.onFirstRender() }

        // loading
        if (isFetchingUserProfiles) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (wasProfileFetchUnsuccessful) {
            Text("Failed to load user profiles!")
        } else if (allActiveUserProfiles.isEmpty()) {
            Text("No user profiles found!")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // loop and make card for each profile
                items(allActiveUserProfiles, key = { it.userId }) { currentUserProfile ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        var profileDisplayName = currentUserProfile.name
                        // empty name exists
                        if (profileDisplayName.isBlank()) { profileDisplayName = "Unnamed User" }

                        Text(text = profileDisplayName, style = MaterialTheme.typography.titleMedium)

                        if (currentUserProfile.hasAnActiveEntrantProfile) { Text("Entrant") }
                        if (currentUserProfile.hasAnActiveCampsiteOwnerProfile) { Text("Campsite Owner") }
                    }
                } // end of loop
            } // end of lazu column
        }
    } // end of render for personal ref
}