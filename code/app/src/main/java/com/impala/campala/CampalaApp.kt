package com.impala.campala

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun CampalaApp() {
    // This is the root of the app
    // In here, we need to create instances of each of our classes (models, views, controllers, etc)
    // And then the navController is how we register our root stack navigation

    var currentViewType = "entrant" // TODO: Figure out actual user type from firebase here (entrant or campsiteowner or admin)
    // More than likely this will need to do like userModel.getAccountType() or something like that
    // And if account type is null, then we need to take them to the screen where they pick an account type
    if (currentViewType == "entrant") {
        EntrantNavigationRoot()
    } else if (currentViewType == "campsiteowner") {
        // TODO: CAMPSITE OWNER UI
    } else if (currentViewType == "admin") {
        // TODO: ADMING UI
    } else {
        // TODO: Intro UI, asking them to choose admin, campsite owner, or entrant, and then assigning that to their user object
    }
}

@Composable
fun EntrantNavigationRoot() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "screen1") {
        composable("screen1") { ExampleScreenOne(navController) }
        composable("screen2") { ExampleScreenTwo(navController) }
    }
}


// Android studio says this @OptIn is required in order to use the TopAppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleScreenTwo(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Screen Two") }) },
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
                    Text("Go To Screen One")
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
        }
    }
}

// Android studio says this @OptIn is required in order to use the TopAppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleScreenOne(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Screen One") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("screen2")
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(modifier = Modifier.padding(horizontal=16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Icon",
                    )
                    Text("Go To Screen Two")
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            UiExamples()
        }
    }
}