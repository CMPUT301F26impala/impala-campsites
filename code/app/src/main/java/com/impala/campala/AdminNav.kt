package com.impala.campala

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.impala.campala.ui_sections.admin_profiles.AdminProfilesView
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.TextButton
import com.impala.campala.ui_sections.view_profile.ViewProfileModel
import com.impala.campala.ui_sections.view_profile.ViewProfileController
import com.impala.campala.ui_sections.view_profile.ViewProfileView
import com.impala.campala.ui_sections.admin_images.AdminImagesView
import com.impala.campala.ui_sections.admin_reviews.AdminReviewsView
import com.impala.campala.ui_sections.campsite_details.CampsiteDetailsView
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesView

/**
 * Composable class that sets up the navigation for a user signed in as an Admin
 *
 * @param searchCampsitesScreen The View where the admin can search for campsites to moderate
 * @param campsiteDetailsScreen The View where the admin can see details of a campsite, delete it, or ban its owner
 * @param adminProfilesScreen view where the admin sees all the user profiles
 * @param adminImagesScreen The View where the admin can see and delete every image
 * @param adminReviewsScreen The View where the admin can see and delete reviews
 * @param onSwitchRole a function to be executed when the user clicks the button to switch roles
 */
@Composable
fun AdminNavigationRoot(
    searchCampsitesScreen: SearchCampsitesView,
    campsiteDetailsScreen: CampsiteDetailsView,
    adminProfilesScreen: AdminProfilesView,
    adminImagesScreen: AdminImagesView,
    adminReviewsScreen: AdminReviewsView,
    onSwitchRole: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // "home" is the tab navigation, the 4 tabs inside of home are controlled by the AdminHomeScreen function
        // Any screens that aren't part of the tabs and are 'fullscreen' should be declared here
        composable("home") { AdminHomeScreen(navController, searchCampsitesScreen, adminProfilesScreen, adminImagesScreen, adminReviewsScreen, onSwitchRole) }
        composable("profile/{userId}") { backStackEntry ->
            val selectedUserProfileId = backStackEntry.arguments?.getString("userId")
            val selectedUserProfile = adminProfilesScreen.controller.model.allActiveUserProfiles.firstOrNull { it.userId == selectedUserProfileId }
            if (selectedUserProfile != null) {
                val adminProfileCoroutineScope = rememberCoroutineScope()
                val adminProfileDetailsModel = remember(selectedUserProfile) { ViewProfileModel(adminProfileCoroutineScope, selectedUserProfile) }
                val adminProfileDetailsController = remember(adminProfileDetailsModel) { ViewProfileController(adminProfileDetailsModel) }

                val adminProfileDetailsView = remember(adminProfileDetailsController) {
                    val newProfileDetailsView = ViewProfileView(adminProfileDetailsController)
                    adminProfileDetailsModel.addObserver(newProfileDetailsView)
                    newProfileDetailsView
                }
                Scaffold { padding ->
                    Box(Modifier.padding(padding).padding(horizontal = 16.dp)) { adminProfileDetailsView.Render(navController) }
                }
            } else {
                Scaffold { padding ->
                    Column(Modifier.padding(padding).padding(16.dp)) {
                        Text("User profile not found!")
                        TextButton(onClick = { navController.popBackStack() }) { Text("Go Back") }
                    }
                }
            }
        } // end of profile composable for personal ref
        composable("campsite/{campsiteId}") { backStackEntry ->
            val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
            Scaffold { padding ->
                Box(Modifier.padding(bottom = padding.calculateBottomPadding())) {
                    campsiteDetailsScreen.Render(navController, campsiteId)
                }
            }
        }
    }
}

enum class AdminTab(val label: String, val icon: ImageVector) {
    Campsites("Campsites", Icons.Default.Search),
    Profiles("Profiles", Icons.Default.AccountCircle),
    Images("Images", Icons.Default.Face),
    Notifs("Alerts", Icons.Default.Notifications),
    Reviews("Reviews", Icons.Default.Star)
}

/**
 * A composable function that generates tabs the user can use to move through the base screens
 *
 * @param navController The navigation controller for the admin section of the app
 * @param searchCampsitesScreen The View where the admin can search for campsites to moderate
 * @param adminProfilesScreen view where the admin sees all the user profiles
 * @param adminImagesScreen The View where the admin can see and delete every image
 * @param adminReviewsScreen The View where the admin can see and delete reviews
 * @param onSwitchRole a function to be executed when the user clicks the button to switch roles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController, searchCampsitesScreen: SearchCampsitesView, adminProfilesScreen: AdminProfilesView, adminImagesScreen: AdminImagesView, adminReviewsScreen: AdminReviewsView, onSwitchRole: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf(AdminTab.Campsites) }
    val tabState = rememberSaveableStateHolder() // keeps each tab's scroll/state
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Icon(
                        painterResource(R.drawable.campala_logo),
                        contentDescription = "Campala",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(20.dp)
                    )
                },
                actions = {
                    // Takes you back to the choose role screen, CampalaApp.kt does the work for that
                    FilledIconButton(onClick = onSwitchRole, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.Person, contentDescription = "Switch user mode")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                AdminTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selected,
                        onClick = { selected = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },)
                }
            }
        }
    ) { padding ->
        // The first padding is the padding required for like the top of the screen and stuff
        // The 16.dp one is to make it so text doesn't touch the very edge pixels, but sits inside a little
        Box(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            tabState.SaveableStateProvider(selected.name) {
                when (selected) {
                    AdminTab.Campsites -> searchCampsitesScreen.Render(navController)
                    AdminTab.Profiles -> adminProfilesScreen.Render(navController)
                    AdminTab.Images -> adminImagesScreen.Render()
                    AdminTab.Notifs -> AdminNotifsTab(navController)
                    AdminTab.Reviews -> adminReviewsScreen.Render()
                }
            }
        }
    }
}

enum class AdminAlertsTab(val label: String) {
    Messages("Messages"),
    Notifications("Notifications")
}

/**
 * Allows you to switch between the Messages and Notifications screens
 *
 * @param navController The navigation controller for the admin section of the app
 */
@Composable
fun AdminNotifsTab(navController: NavController) {
    var selected by rememberSaveable { mutableStateOf(AdminAlertsTab.Messages) }
    val tabState = rememberSaveableStateHolder() // keeps each tab's scroll/state
    Column() {
        PrimaryTabRow(selectedTabIndex = selected.ordinal) {
            AdminAlertsTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selected,
                    onClick = { selected = tab },
                    text = { Text(text = tab.label, maxLines = 1) }
                )
            }
        }
        tabState.SaveableStateProvider(selected.name) {
            when (selected) {
                // TODO: swap these placeholders for the Messages and Notifications views once they exist
                AdminAlertsTab.Messages -> Text("Messages")
                AdminAlertsTab.Notifications -> Text("Notifications")
            }
        }
    }
}
