package com.impala.campala

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesView

@Composable
fun EntrantNavigationRoot(searchCampsitesScreen: SearchCampsitesView) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // "home" is the tab navigation, the 4 tabs inside of home are controlled by the EntrantHomeScreen function
        // Any screens that aren't part of the tabs and are 'fullscreen' should be declared here
        composable("home") { EntrantHomeScreen(navController, searchCampsitesScreen) }
        composable("screen1") { ExampleScreenOne(navController) }
    }
}

enum class EntrantTab(val label: String, val icon: ImageVector) {
    Search("Search", Icons.Default.Search),
    Lotteries("Lotteries", Icons.Default.MailOutline),
    Reservations("Reservations", Icons.Default.DateRange),
    Profile("Profile", Icons.Default.Person)
}


// Because nested navigators is hard, the decision I made for tabs
// (Still subject to change in the future) is that the home page tab nav
// Will just be a piece of local state in the HomeScreen, the navbar screens aren't real screens in the stack
// In researching how this is normally done, I did use AI
// I prompted Claude Opus 5.5 in the claude.ai web app "In a Jetpack Compose android app, what's the best way to have a stack nav with the home page having a tab nav inside of it"
// It suggested this approach and the rememberSavable and rememberSavableStateHolder functions, and from there I implemented it conforming to that suggestion
// This may change in the future into a 100% human-derived solution, but I'm just disclosing this for transaprency
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrantHomeScreen(navController: NavController, searchCampsitesScreen: SearchCampsitesView) {
    var selected by rememberSaveable { mutableStateOf(EntrantTab.Search) }
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
                    // TODO: switch between entrant and campsite owner
                    FilledIconButton(onClick = {}, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.Person, contentDescription = "Switch user mode")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                EntrantTab.entries.forEach { tab ->
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
                    EntrantTab.Search -> searchCampsitesScreen.Render()
                    EntrantTab.Lotteries -> LotteriesTab(navController)
                    EntrantTab.Reservations -> ReservationsTab(navController)
                    EntrantTab.Profile -> ProfileTab(navController)
                }
            }
        }
    }
}

@Composable
fun SearchTab(navController: NavController) {
    Column() {
        Text("Search Screen!!!!")
        Button(onClick = {
            navController.navigate("screen1")
        }) {
            Text("Example Button Taking You To Another Screen")
        }
    }
}

@Composable
fun LotteriesTab(navController: NavController) {
    Column() {
        Text("Lotteries Screen!!!!")
    }
}

@Composable
fun ReservationsTab(navController: NavController) {
    Column() {
        Text("Reservations Screen!!!!")
    }
}

@Composable
fun ProfileTab(navController: NavController) {
    Column() {
        Text("Profile Screen!!!!")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleScreenOne(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Example Components Screen") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.popBackStack()
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(modifier = Modifier.padding(horizontal=16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Icon",
                    )
                    Text("Go Back!")
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).padding(horizontal=16.dp)) {
            UiExamples()
        }
    }
}