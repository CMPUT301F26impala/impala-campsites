package com.impala.campala

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.impala.campala.ui_sections.campsite_details.CampsiteDetailsView
import com.impala.campala.ui_sections.form_campsites.FormCampsitesView
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesView

@Composable
fun CampsiteOwnerNavigationRoot(
    searchCampsitesScreen: SearchCampsitesView,
    campsiteDetailsScreen: CampsiteDetailsView,
    formCampsitesScreen: FormCampsitesView,
    onSwitchRole: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // "home" is the tab navigation, the 4 tabs inside of home are controlled by the CampsiteOwnerHomeScreen function
        // Any screens that aren't part of the tabs and are 'fullscreen' should be declared here
        composable("home") { CampsiteOwnerHomeScreen(navController, searchCampsitesScreen, onSwitchRole) }
        composable("campsite/{campsiteId}") { backStackEntry ->
            val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
            // Scaffold is just here so the screen respects the status bar / nav bar insets
            Scaffold { padding ->
                Box(Modifier.padding(padding)) {
                    campsiteDetailsScreen.Render(navController, campsiteId)
                }
            }
        }
        composable("addCampsite") {
            Scaffold { padding ->
                Box(Modifier.padding(padding)) {
                    formCampsitesScreen.Render(navController)
                }
            }
        }
    }
}

enum class CampsiteOwnerTab(val label: String, val icon: ImageVector) {
    Campsites("Campsites", Icons.Default.Search),
    Lotteries("Lotteries", Icons.Default.MailOutline),
    Reservations("Reservations", Icons.Default.DateRange),
    Notifs("Notifs", Icons.Default.Notifications)
}

// Same tab approach as EntrantHomeScreen, the tabs are just local state instead of real screens in NavController
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteOwnerHomeScreen(navController: NavController, searchCampsitesScreen: SearchCampsitesView, onSwitchRole: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf(CampsiteOwnerTab.Campsites) }
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
                CampsiteOwnerTab.entries.forEach { tab ->
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
                    CampsiteOwnerTab.Campsites -> searchCampsitesScreen.Render(navController)
                    CampsiteOwnerTab.Lotteries -> LotteriesTab(navController)
                    CampsiteOwnerTab.Reservations -> ReservationsTab(navController)
                    CampsiteOwnerTab.Notifs -> NotifsTab(navController)
                }
            }
        }
    }
}

@Composable
fun NotifsTab(navController: NavController) {
    Column() {
        Text("Notifs Screen!!!!")
    }
}
