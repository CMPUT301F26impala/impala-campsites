package com.impala.campala.ui_sections.admin_profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.impala.campala.ImageDisplay
import com.impala.campala.ViewObserver
import com.impala.campala.model.AdminProfileModel
import androidx.compose.foundation.clickable
import androidx.navigation.NavController

/**
 * View for the profiles tab of the admin.
 *
 * Displays the list of active user profiles fetched by the model.
 * Receives changes from the model and sends user actions to the controller.
 * Allows the admin to search through the user profiles.
 *
 * @param controller the AdminProfilesController used to send actions to the model
 */
class AdminProfilesView(
    val controller: AdminProfilesController
) : ViewObserver<AdminProfilesModel> {

    private var filteredUserProfiles by mutableStateOf(listOf<AdminProfileModel>())
    private var isFetchingUserProfiles by mutableStateOf(false)
    private var wasProfileFetchUnsuccessful by mutableStateOf(false)
    private var profileSearchInput by mutableStateOf("")
    private var profilePictureBytesById by mutableStateOf(mapOf<String, ByteArray>())

    /**
     * Moves profile details from the model into the ui
     *
     * @param model the model containing the required details
     */
    override fun update(model: AdminProfilesModel) {
        filteredUserProfiles = model.filteredUserProfiles
        isFetchingUserProfiles = model.isFetchingUserProfiles
        wasProfileFetchUnsuccessful = model.wasProfileFetchUnsuccessful
        profileSearchInput = model.profileSearchInput
        profilePictureBytesById = model.profilePictureBytesById
    }

    init { update(controller.model) }

    /**
     * Outputs the admin profiles screen ui and loads whenever the tab is opened.
     *
     * @param navController the navigation controller
     *
     * @return Unit
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Render(navController: NavController) {
        // first look at the ui
        LaunchedEffect(Unit) { controller.onFirstRender() }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // search bar
            SearchBar(
                modifier = Modifier.fillMaxWidth(),
                expanded = false,
                onExpandedChange = {},
                inputField = {
                    SearchBarDefaults.InputField(
                        query = profileSearchInput,
                        onQueryChange = { controller.onSearchBarInputChange(it) },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text("Search") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") }
                    )
                }
            ) {}

            // loading
            if (isFetchingUserProfiles) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (wasProfileFetchUnsuccessful) {
                Text("Failed to load user profiles!")
            } else if (filteredUserProfiles.isEmpty()) {
                Text("No user profiles found!")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // loop and make row for each profile
                    items(filteredUserProfiles, key = { it.userId }) { currentUserProfile ->
                        DrawUserProfileRow(currentUserProfile, navController)
                        HorizontalDivider()
                    } // end of loop
                } // end of lazy column
            }
        }
    } // end of render for personal ref

    /**
     * Displays all details of a user, also whether they are an entrant, campsite owner or both
     *
     * @param currentUserProfile the profile to be displayed
     */
    @Composable
    private fun DrawUserProfileRow(currentUserProfile: AdminProfileModel, navController: NavController) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                .clickable { controller.onUserProfilePressed(navController, currentUserProfile.userId) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DrawUserProfilePicture(currentUserProfile.imageId)

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                var profileDisplayName = currentUserProfile.name

                // empty name exists
                if (profileDisplayName.isBlank()) { profileDisplayName = "Unnamed User" }
                Text(text = profileDisplayName, style = MaterialTheme.typography.titleMedium)
                Text(text = currentUserProfile.email, style = MaterialTheme.typography.bodyMedium)
                Text(text = currentUserProfile.phoneNumber, style = MaterialTheme.typography.bodyMedium)

                var activeProfileRoleText = ""
                // check which roles are active for the user
                if (currentUserProfile.hasAnActiveEntrantProfile && currentUserProfile.hasAnActiveCampsiteOwnerProfile) {
                    activeProfileRoleText = "Entrant, Campsite Owner"
                } else if (currentUserProfile.hasAnActiveEntrantProfile) {
                    activeProfileRoleText = "Entrant"
                } else if (currentUserProfile.hasAnActiveCampsiteOwnerProfile) {
                    activeProfileRoleText = "Campsite Owner"
                }

                Text(
                    text = activeProfileRoleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } // end of profile row for personal ref

    /**
     * Shows profile picture of the user or blank box if there isn't one
     *
     * @param profilePictureImageId the image ID of the user's profile picture
     */
    @Composable
    private fun DrawUserProfilePicture(profilePictureImageId: String) {
        val profilePictureModifier = Modifier.size(64.dp).clip(CircleShape)
        val currentProfilePictureBytes = profilePictureBytesById[profilePictureImageId]

        // if a profile picture exists great, or else defult box
        if (currentProfilePictureBytes != null) {
            ImageDisplay(imageBytes = currentProfilePictureBytes, modifier = profilePictureModifier)
        } else {
            Box(modifier = profilePictureModifier.background(Brush.verticalGradient(listOf(Color.White, Color(0xFF9E9E9E)))))
        }
    } // end of profile picture for personal ref
}