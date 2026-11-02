package com.impala.campala

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesController
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesModel
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesView
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesViewMode
import com.impala.campala.ui_sections.form_campsites.FormCampsitesModel
import com.impala.campala.ui_sections.form_campsites.FormCampsitesController
import com.impala.campala.ui_sections.form_campsites.FormCampsitesView
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.CampsiteOwnerProfileRepository
import com.impala.campala.model.EntrantProfileRepository
import com.impala.campala.model.LotteryRepository
import com.impala.campala.ui_sections.campsite_details.CampsiteDetailsController
import com.impala.campala.ui_sections.campsite_details.CampsiteDetailsModel
import com.impala.campala.ui_sections.campsite_details.CampsiteDetailsView
import com.impala.campala.ui_sections.choose_role.ChooseRoleController
import com.impala.campala.ui_sections.choose_role.ChooseRoleModel
import com.impala.campala.ui_sections.choose_role.ChooseRoleView


@Composable
fun CampalaApp() {
    // This is the root of the app
    // In here, we need to create instances of each of our classes (models, views, controllers, etc)
    // And then the navController is how we register our root stack navigation

    // Get (or create) the User record for this device as soon as the app starts
    // Anything that needs the entrant profile for this device can do entrantProfileRepo.getEntrantProfileById(user.getUID())
    var user by remember { mutableStateOf<User?>(null) }
    // This is just a property on user, but it's cleaner to extract it out
    var currentRole by remember { mutableStateOf<UserViewMode?>(null) }
    // This is true either if it's first-app-open or if the user requested to change view modes / roles
    var needsRoleChoice by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val loadedUser = User().initialize()
        currentRole = loadedUser.getCurrentRole()
        // If they have never had an entrant or campsite owner profile, they're new and should get the welcome screen
        needsRoleChoice = !loadedUser.getHasCreatedEntrantProfile() && !loadedUser.getHasCreatedCampsiteOwnerProfile()
        user = loadedUser
    }

    // Here, remember basically means "make this one, and even if this rerenders use the old version"
    // That way, we aren't recreating these classes a bunch of times.
    val coroutineScope = rememberCoroutineScope()
    val campsiteRepo = remember { CampsiteRepository() }
    val entrantProfileRepo = remember { EntrantProfileRepository() }
    val campsiteOwnerProfileRepo = remember { CampsiteOwnerProfileRepository() }
    val lotteryRepo = remember { LotteryRepository() }

    val searchCampsitesModel = remember { SearchCampsitesModel(campsiteRepo, coroutineScope, SearchCampsitesViewMode.ENTRANT) }
    val searchCampsitesController = remember { SearchCampsitesController(searchCampsitesModel) }
    val searchCampsitesView = remember { SearchCampsitesView(searchCampsitesController) }
    searchCampsitesModel.addObserver(searchCampsitesView)

    val formCampsitesModel = remember { FormCampsitesModel(campsiteRepo, coroutineScope) }
    val formCampsitesController = remember { FormCampsitesController(formCampsitesModel) }
    val formCampsitesView = remember { FormCampsitesView(formCampsitesController) }
    formCampsitesModel.addObserver((formCampsitesView))

    val campsiteDetailsModel = remember { CampsiteDetailsModel(campsiteRepo, lotteryRepo, entrantProfileRepo, user, coroutineScope) }
    val campsiteDetailsController = remember { CampsiteDetailsController(campsiteDetailsModel) }
    val campsiteDetailsView = remember { CampsiteDetailsView(campsiteDetailsController) }
    campsiteDetailsModel.addObserver(campsiteDetailsView)

    val chooseRoleModel = remember { ChooseRoleModel(entrantProfileRepo, campsiteOwnerProfileRepo, coroutineScope) }
    val chooseRoleController = remember { ChooseRoleController(chooseRoleModel) }
    val chooseRoleView = remember { ChooseRoleView(chooseRoleController) }
    chooseRoleModel.addObserver(chooseRoleView)

    user?.let { // This is kotlin code equivalent to: if user == null do nothing, else switch case state on the role
        if (needsRoleChoice) {
            // Scaffold is just here so the screen respects the status bar / nav bar insets
            Scaffold { padding ->
                Box(Modifier.padding(padding)) {
                    chooseRoleView.Render(it, onRoleChosen = { role ->
                        currentRole = role
                        needsRoleChoice = false
                    })
                }
            }
        } else {
            when (currentRole) {
                UserViewMode.ENTRANT -> EntrantNavigationRoot(searchCampsitesScreen=searchCampsitesView, campsiteDetailsScreen=campsiteDetailsView, onSwitchRole={ needsRoleChoice = true })
                UserViewMode.CAMPSITE_OWNER -> {
                    Button(onClick = { needsRoleChoice = true}) {
                        Text("Go Back, Not Done Yet")
                    }
                    // TODO: CAMPSITE OWNER U I
                }

                UserViewMode.ADMIN -> {
                    Button(onClick = { needsRoleChoice = true}) {
                        Text("Go Back, Not Done Yet")
                    }
                    // TODO: ADMIN UI
                }

                null -> {
                    // They have a profile but no role saved, so just send them to the entrant UI so the app is still usable
                    EntrantNavigationRoot(searchCampsitesScreen=searchCampsitesView, campsiteDetailsScreen=campsiteDetailsView, onSwitchRole={ needsRoleChoice = true })
                }
            }
        }

    }


}