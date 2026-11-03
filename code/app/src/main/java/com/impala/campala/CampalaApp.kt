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
import com.impala.campala.ui_sections.form_lotteries.FormLotteriesController
import com.impala.campala.ui_sections.form_lotteries.FormLotteriesModel
import com.impala.campala.ui_sections.form_lotteries.FormLotteriesView


@Composable
fun CampalaApp() {
    // This is the root of the app
    // What this does is just set up the User, and then SignedInApp does the rest
    // That way, the entire rest of the code can just assume User is not null and is already initialized.
    // Makes our lives a lot easier.
    var user by remember { mutableStateOf<User?>(null) }
    LaunchedEffect(Unit) {
        user = User().initialize()
    }

    user?.let { SignedInApp(it) } // Nothing is shown until the user has loaded
}

@Composable
fun SignedInApp(user: User) {
    // This is just a property on user, but it's cleaner to extract it out
    var currentRole by remember { mutableStateOf(user.getCurrentRole()) }
    // This is true either if it's first-app-open or if the user requested to change view modes / roles
    // If they have never had an entrant or campsite owner profile, they're new and should start on the choose role screen
    var needsRoleChoice by remember { mutableStateOf(!user.getHasCreatedEntrantProfile() && !user.getHasCreatedCampsiteOwnerProfile()) }

    // Here, remember basically means "make this one, and even if this rerenders use the old version"
    // That way, we aren't recreating these classes a bunch of times.
    val coroutineScope = rememberCoroutineScope()
    val campsiteRepo = remember { CampsiteRepository() }
    val entrantProfileRepo = remember { EntrantProfileRepository() }
    val campsiteOwnerProfileRepo = remember { CampsiteOwnerProfileRepository() }
    val lotteryRepo = remember { LotteryRepository() }

    val searchCampsitesModel = remember { SearchCampsitesModel(campsiteRepo, campsiteOwnerProfileRepo, user, coroutineScope, SearchCampsitesViewMode.ENTRANT) }
    val searchCampsitesController = remember { SearchCampsitesController(searchCampsitesModel) }
    val searchCampsitesView = remember { SearchCampsitesView(searchCampsitesController) }
    searchCampsitesModel.addObserver(searchCampsitesView)

    // Same screen as above, but in campsite owner mode it only shows the user's own campsites and has the Add Campsite button
    val ownerSearchCampsitesModel = remember { SearchCampsitesModel(campsiteRepo, campsiteOwnerProfileRepo, user, coroutineScope, SearchCampsitesViewMode.CAMPSITE_OWNER) }
    val ownerSearchCampsitesController = remember { SearchCampsitesController(ownerSearchCampsitesModel) }
    val ownerSearchCampsitesView = remember { SearchCampsitesView(ownerSearchCampsitesController) }
    ownerSearchCampsitesModel.addObserver(ownerSearchCampsitesView)

    val formCampsitesModel = remember { FormCampsitesModel(campsiteRepo, campsiteOwnerProfileRepo, user, coroutineScope) }
    val formCampsitesController = remember { FormCampsitesController(formCampsitesModel) }
    val formCampsitesView = remember { FormCampsitesView(formCampsitesController) }
    formCampsitesModel.addObserver((formCampsitesView))

    val formLotteriesModel = remember { FormLotteriesModel(lotteryRepo, coroutineScope, null, user.getUID(), campsiteRepo) }
    val formLotteriesController = remember { FormLotteriesController(formLotteriesModel) }
    val formLotteriesView = remember { FormLotteriesView(formLotteriesController) }
    formLotteriesModel.addObserver(formLotteriesView)

    val campsiteDetailsModel = remember { CampsiteDetailsModel(campsiteRepo, lotteryRepo, entrantProfileRepo, campsiteOwnerProfileRepo, user, coroutineScope) }
    val campsiteDetailsController = remember { CampsiteDetailsController(campsiteDetailsModel) }
    val campsiteDetailsView = remember { CampsiteDetailsView(campsiteDetailsController) }
    campsiteDetailsModel.addObserver(campsiteDetailsView)

    val chooseRoleModel = remember { ChooseRoleModel(entrantProfileRepo, campsiteOwnerProfileRepo, coroutineScope) }
    val chooseRoleController = remember { ChooseRoleController(chooseRoleModel) }
    val chooseRoleView = remember { ChooseRoleView(chooseRoleController) }
    chooseRoleModel.addObserver(chooseRoleView)

    if (needsRoleChoice) {
        // Scaffold is just here so the screen respects the status bar / nav bar insets
        Scaffold { padding ->
            Box(Modifier.padding(padding)) {
                chooseRoleView.Render(user, onRoleChosen = { role ->
                    currentRole = role
                    needsRoleChoice = false
                })
            }
        }
    } else {
        when (currentRole) {
            UserViewMode.ENTRANT -> EntrantNavigationRoot(searchCampsitesScreen=searchCampsitesView, campsiteDetailsScreen=campsiteDetailsView, onSwitchRole={ needsRoleChoice = true })
            UserViewMode.CAMPSITE_OWNER -> CampsiteOwnerNavigationRoot(searchCampsitesScreen=ownerSearchCampsitesView, campsiteDetailsScreen=campsiteDetailsView, formCampsitesScreen=formCampsitesView, formLotteriesScreen = formLotteriesView,  onSwitchRole={ needsRoleChoice = true })

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
