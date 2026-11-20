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
import androidx.compose.material3.Scaffold
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

/**
 * Composable class that sets up the navigation for a user signed in as an Admin
 *
 * @param onSwitchRole a function to be executed when the user clicks the button to switch roles
 */
@Composable
fun AdminNavigationRoot(
    onSwitchRole: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // "home" is the tab navigation, the 4 tabs inside of home are controlled by the AdminHomeScreen function
        // Any screens that aren't part of the tabs and are 'fullscreen' should be declared here
        composable("home") { AdminHomeScreen(navController, onSwitchRole) }
    }
}

enum class AdminTab(val label: String, val icon: ImageVector) {
    Campsites("Campsites", Icons.Default.Search),
    Profiles("Profiles", Icons.Default.AccountCircle),
    Images("Images", Icons.Default.Face),
    Notifs("Messages", Icons.Default.Notifications),
    Reviews("Reviews", Icons.Default.Star)
}

/**
 * A composable function that generates tabs the user can use to move through the base screens
 *
 * @param navController The navigation controller for the admin section of the app
 * @param onSwitchRole a function to be executed when the user clicks the button to switch roles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController, onSwitchRole: () -> Unit) {
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
                    AdminTab.Campsites -> AdminCampsitesTab(navController)
                    AdminTab.Profiles -> AdminProfilesTab(navController)
                    AdminTab.Images -> AdminImagesTab(navController)
                    AdminTab.Notifs -> AdminNotifsTab(navController)
                    AdminTab.Reviews -> AdminReviewsTab(navController)
                }
            }
        }
    }
}

@Composable
fun AdminCampsitesTab(navController: NavController) {
    Column() {
    }
}

@Composable
fun AdminProfilesTab(navController: NavController) {
    Column() {
    }
}

@Composable
fun AdminImagesTab(navController: NavController) {
    Column() {
    }
}

@Composable
fun AdminNotifsTab(navController: NavController) {
    Column() {
    }
}

@Composable
fun AdminReviewsTab(navController: NavController) {
    Column() {
    }
}
