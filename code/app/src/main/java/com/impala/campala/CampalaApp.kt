package com.impala.campala

import androidx.compose.runtime.Composable

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