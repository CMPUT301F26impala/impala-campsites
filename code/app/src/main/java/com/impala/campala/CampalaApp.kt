package com.impala.campala

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesController
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesModel
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesView
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesViewMode
import com.impala.campala.ui_sections.form_campsites.FormCampsitesModel
import com.impala.campala.ui_sections.form_campsites.FormCampsitesController
import com.impala.campala.ui_sections.form_campsites.FormCampsitesView
import com.impala.campala.model.CampsiteRepository


@Composable
fun CampalaApp() {
    // This is the root of the app
    // In here, we need to create instances of each of our classes (models, views, controllers, etc)
    // And then the navController is how we register our root stack navigation

    // Here, remember basically means "make this one, and even if this rerenders use the old version"
    // That way, we aren't recreating these classes a bunch of times.
    val coroutineScope = rememberCoroutineScope()
    val campsiteRepo = remember { CampsiteRepository() }

    val searchCampsitesModel = remember { SearchCampsitesModel(campsiteRepo, coroutineScope, SearchCampsitesViewMode.ENTRANT) }
    val searchCampsitesController = remember { SearchCampsitesController(searchCampsitesModel) }
    val searchCampsitesView = remember { SearchCampsitesView(searchCampsitesController) }
    searchCampsitesModel.addObserver(searchCampsitesView)

    val formCampsitesModel = remember { FormCampsitesModel(campsiteRepo, coroutineScope) }
    val formCampsitesController = remember { FormCampsitesController(formCampsitesModel) }
    val formCampsitesView = remember { FormCampsitesView(formCampsitesController) }
    formCampsitesModel.addObserver((formCampsitesView))

    var currentViewType = "entrant" // TODO: Figure out actual user type from firebase here (entrant or campsiteowner or admin)
    // More than likely this will need to do like userModel.getAccountType() or something like that
    // And if account type is null, then we need to take them to the screen where they pick an account type
    if (currentViewType == "entrant") {
        EntrantNavigationRoot(searchCampsitesScreen=searchCampsitesView)
    } else if (currentViewType == "campsiteowner") {
        // TODO: CAMPSITE OWNER UI
    } else if (currentViewType == "admin") {
        // TODO: ADMING UI
    } else {
        // TODO: Intro UI, asking them to choose admin, campsite owner, or entrant, and then assigning that to their user object
    }
}