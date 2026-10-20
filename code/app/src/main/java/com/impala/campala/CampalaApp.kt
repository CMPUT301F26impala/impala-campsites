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