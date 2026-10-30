package com.impala.campala

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesController
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesModel
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesView
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesViewMode
import com.impala.campala.ui_sections.form_campsites.FormCampsitesModel
import com.impala.campala.ui_sections.form_campsites.FormCampsitesController
import com.impala.campala.ui_sections.form_campsites.FormCampsitesView
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.EntrantProfileRepository


@Composable
fun CampalaApp() {
    // This is the root of the app
    // In here, we need to create instances of each of our classes (models, views, controllers, etc)
    // And then the navController is how we register our root stack navigation

    // Here, remember basically means "make this one, and even if this rerenders use the old version"
    // That way, we aren't recreating these classes a bunch of times.
    val coroutineScope = rememberCoroutineScope()
    val campsiteRepo = remember { CampsiteRepository() }
    val entrantProfileRepo = remember { EntrantProfileRepository() }

    val searchCampsitesModel = remember { SearchCampsitesModel(campsiteRepo, coroutineScope, SearchCampsitesViewMode.ENTRANT) }
    val searchCampsitesController = remember { SearchCampsitesController(searchCampsitesModel) }
    val searchCampsitesView = remember { SearchCampsitesView(searchCampsitesController) }
    searchCampsitesModel.addObserver(searchCampsitesView)

    val formCampsitesModel = remember { FormCampsitesModel(campsiteRepo, coroutineScope) }
    val formCampsitesController = remember { FormCampsitesController(formCampsitesModel) }
    val formCampsitesView = remember { FormCampsitesView(formCampsitesController) }
    formCampsitesModel.addObserver((formCampsitesView))

    // Get (or create) the User record for this device as soon as the app starts
    // Anything that needs the entrant profile for this device can do entrantProfileRepo.getEntrantProfileById(user.getUID())
    var user by remember { mutableStateOf<User?>(null) }
    LaunchedEffect(Unit) {
        user = User().initialize()
    }

    user?.let { // This is kotlin code equivalent to: if user == null do nothing, else switch case state on the role
        when (it.getCurrentRole()) {
            UserViewMode.ENTRANT -> EntrantNavigationRoot(searchCampsitesScreen=searchCampsitesView)
            UserViewMode.CAMPSITE_OWNER -> {
                // TODO: CAMPSITE OWNER UI
            }

            UserViewMode.ADMIN -> {
                // TODO: ADMING UI
            }

            null -> {
                // TODO: Intro UI, asking them to choose admin, campsite owner, or entrant, and then assigning that to their user object
                // Until that exists, just send them to the entrant UI so the app is still usable
                EntrantNavigationRoot(searchCampsitesScreen=searchCampsitesView)
            }
        }
    }


}