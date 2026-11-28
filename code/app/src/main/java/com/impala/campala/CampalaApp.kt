package com.impala.campala

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.impala.campala.ui_sections.admin_images.AdminImagesController
import com.impala.campala.ui_sections.admin_images.AdminImagesModel
import com.impala.campala.ui_sections.admin_images.AdminImagesView
import com.impala.campala.ui_sections.admin_reviews.AdminReviewsController
import com.impala.campala.ui_sections.admin_reviews.AdminReviewsModel
import com.impala.campala.ui_sections.admin_reviews.AdminReviewsView
import com.impala.campala.ui_sections.admin_messages.AdminMessagesController
import com.impala.campala.ui_sections.admin_messages.AdminMessagesModel
import com.impala.campala.ui_sections.admin_messages.AdminMessagesView
import com.impala.campala.ui_sections.campsite_details.CampsiteDetailsController
import com.impala.campala.ui_sections.campsite_details.CampsiteDetailsModel
import com.impala.campala.ui_sections.campsite_details.CampsiteDetailsView
import com.impala.campala.ui_sections.choose_role.ChooseRoleController
import com.impala.campala.ui_sections.choose_role.ChooseRoleModel
import com.impala.campala.ui_sections.choose_role.ChooseRoleView
import com.impala.campala.ui_sections.create_reservation.CreateReservationController
import com.impala.campala.ui_sections.create_reservation.CreateReservationModel
import com.impala.campala.ui_sections.create_reservation.CreateReservationView
import com.impala.campala.ui_sections.edit_profile.EditProfileController
import com.impala.campala.ui_sections.edit_profile.EditProfileModel
import com.impala.campala.ui_sections.edit_profile.EditProfileView
import com.impala.campala.ui_sections.form_campsites.FormCampsitesController
import com.impala.campala.ui_sections.form_campsites.FormCampsitesModel
import com.impala.campala.ui_sections.form_campsites.FormCampsitesView
import com.impala.campala.ui_sections.form_lotteries.FormLotteriesController
import com.impala.campala.ui_sections.form_lotteries.FormLotteriesModel
import com.impala.campala.ui_sections.form_lotteries.FormLotteriesView
import com.impala.campala.ui_sections.lottery_details.LotteryDetailsController
import com.impala.campala.ui_sections.lottery_details.LotteryDetailsModel
import com.impala.campala.ui_sections.lottery_details.LotteryDetailsView
import com.impala.campala.ui_sections.owner_notifs.OwnerNotifsController
import com.impala.campala.ui_sections.owner_notifs.OwnerNotifsModel
import com.impala.campala.ui_sections.owner_notifs.OwnerNotifsView
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesController
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesModel
import com.impala.campala.ui_sections.search_campsites.SearchCampsitesView
import com.impala.campala.ui_sections.see_reservation.SeeReservationsController
import com.impala.campala.ui_sections.see_reservation.SeeReservationsModel
import com.impala.campala.ui_sections.see_reservation.SeeReservationsView
import com.impala.campala.ui_sections.view_lotteries.ViewLotteriesController
import com.impala.campala.ui_sections.view_lotteries.ViewLotteriesModel
import com.impala.campala.ui_sections.view_lotteries.ViewLotteriesView
import com.impala.campala.ui_sections.view_profile.ViewProfileController
import com.impala.campala.ui_sections.view_profile.ViewProfileModel
import com.impala.campala.ui_sections.view_profile.ViewProfileView
import com.impala.campala.ui_sections.admin_profiles.AdminProfilesModel
import com.impala.campala.ui_sections.admin_profiles.AdminProfilesController
import com.impala.campala.ui_sections.admin_profiles.AdminProfilesView
import com.impala.campala.ui_sections.message_user.MessageUserController
import com.impala.campala.ui_sections.message_user.MessageUserModel
import com.impala.campala.ui_sections.message_user.MessageUserView

/**
 * Composable function that acts as the root of the app
 * It sets up the User and SignedInApp does the rest
 * That way, the rest of the code can just assume User is not null and is already initialized
 *
 * @param deepLinkCampsiteId If there was a deep link opened, this is the link
 * @param onDeepLinkHandled Called once the app has navigated to the deep linked campsite to clear the value
 */
@Composable
fun CampalaApp(deepLinkCampsiteId: String? = null, onDeepLinkHandled: () -> Unit = {}) {
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        User.initialize()
        isInitialized = true
    }
    // SignedInApp only shows once the User is initialized, so the deep link can't be overwritten by
    // initialize() loading the saved role from the server
    if (isInitialized) {
        SignedInApp(deepLinkCampsiteId = deepLinkCampsiteId, onDeepLinkHandled = onDeepLinkHandled)
    } else {
        // TODO: loading screen
        Box(modifier = Modifier.fillMaxSize())
    }
}

/**
 * A composable function that initializes the screens and directs the User to a Nav based on their role
 *
 * @param deepLinkCampsiteId The campsite a deep link asked to open, the User is switched to entrant mode to view it
 * @param onDeepLinkHandled Called once the app has navigated to the deep linked campsite
 */
@Composable
fun SignedInApp(deepLinkCampsiteId: String? = null, onDeepLinkHandled: () -> Unit = {}) {
    // This is just a property on User, but it's cleaner to extract it out
    var currentRole by remember { mutableStateOf(User.getCurrentRole()) }
    // This is true either if it's first-app-open or if the User requested to change view modes / roles
    // If they have never had an entrant or campsite owner profile, they're new and should start on the choose role screen, also check if profile is deactivated
    // A banned campsite owner also has to pick a different role
    var needsRoleChoice by remember { mutableStateOf(
        (!User.getHasCreatedEntrantProfile() && !User.getHasCreatedCampsiteOwnerProfile())
            || (currentRole == UserViewMode.ENTRANT && !User.getHasActiveEntrantProfile())
            || (currentRole == UserViewMode.CAMPSITE_OWNER && !User.getHasActiveCampsiteOwnerProfile())
            || currentRole == null
    ) }

    // Here, remember basically means "make this one, and even if this rerenders use the old version"
    // That way, we aren't recreating these classes a bunch of times.
    val coroutineScope = rememberCoroutineScope()
    val contentResolver = LocalContext.current.contentResolver
    val imageCompressor = remember { ImageCompressor(contentResolver) }

    // Shared by all three navs, it checks User.getCurrentRole() to decide between the entrant, campsite owner, and admin UI
    val searchCampsitesModel = remember { SearchCampsitesModel(coroutineScope) }
    val searchCampsitesController = remember { SearchCampsitesController(searchCampsitesModel) }
    val searchCampsitesView = remember { SearchCampsitesView(searchCampsitesController) }
    searchCampsitesModel.addObserver(searchCampsitesView)

    val formCampsitesModel = remember { FormCampsitesModel(imageCompressor, coroutineScope) }
    val formCampsitesController = remember { FormCampsitesController(formCampsitesModel) }
    val formCampsitesView = remember { FormCampsitesView(formCampsitesController) }
    formCampsitesModel.addObserver((formCampsitesView))

    val formLotteriesModel = remember { FormLotteriesModel(coroutineScope, null, User.getUID()) }
    val formLotteriesController = remember { FormLotteriesController(formLotteriesModel) }
    val formLotteriesView = remember { FormLotteriesView(formLotteriesController) }
    formLotteriesModel.addObserver(formLotteriesView)

    val campsiteDetailsModel = remember { CampsiteDetailsModel(coroutineScope) }
    val campsiteDetailsController = remember { CampsiteDetailsController(campsiteDetailsModel) }
    val campsiteDetailsView = remember { CampsiteDetailsView(campsiteDetailsController) }
    campsiteDetailsModel.addObserver(campsiteDetailsView)

    val chooseRoleModel = remember { ChooseRoleModel(coroutineScope) }
    val chooseRoleController = remember { ChooseRoleController(chooseRoleModel) }
    val chooseRoleView = remember { ChooseRoleView(chooseRoleController) }
    chooseRoleModel.addObserver(chooseRoleView)

    // Shared by both navs, it checks User.getCurrentRole() to decide between the entrant and campsite owner UI
    val viewLotteriesModel = remember { ViewLotteriesModel(coroutineScope) }
    val viewLotteriesController = remember { ViewLotteriesController(viewLotteriesModel) }
    val viewLotteriesView = remember { ViewLotteriesView(viewLotteriesController) }
    viewLotteriesModel.addObserver(viewLotteriesView)

    val createReservationModel = remember { CreateReservationModel(coroutineScope) }
    val createReservationController =
        remember { CreateReservationController(createReservationModel) }
    val createReservationView = remember { CreateReservationView(createReservationController) }
    createReservationModel.addObserver(createReservationView)

    val seeReservationsModel = remember { SeeReservationsModel(coroutineScope) }
    val seeReservationsController = remember { SeeReservationsController(seeReservationsModel) }
    val seeReservationsView = remember { SeeReservationsView(seeReservationsController) }
    seeReservationsModel.addObserver(seeReservationsView)

    val viewProfileModel = remember { ViewProfileModel(coroutineScope) }
    val viewProfileController = remember { ViewProfileController(viewProfileModel) }
    val viewProfileView = remember { ViewProfileView(viewProfileController) }
    viewProfileModel.addObserver(viewProfileView)

    val adminProfilesModel = remember { AdminProfilesModel(coroutineScope) }
    val adminProfilesController = remember { AdminProfilesController(adminProfilesModel) }
    val adminProfilesView = remember {
        val newAdminProfilesView = AdminProfilesView(adminProfilesController)
        adminProfilesModel.addObserver(newAdminProfilesView)
        newAdminProfilesView
    }

    val editProfileModel = remember { EditProfileModel(imageCompressor, coroutineScope) }
    val editProfileController = remember { EditProfileController(editProfileModel) }
    val editProfileView = remember { EditProfileView(editProfileController) }
    editProfileModel.addObserver(editProfileView)

    val lotteryDetailsModel = remember { LotteryDetailsModel() }
    val lotteryDetailsController = remember { LotteryDetailsController(lotteryDetailsModel) }
    val lotteryDetailsView = remember { LotteryDetailsView(lotteryDetailsController) }
    lotteryDetailsModel.addObserver(lotteryDetailsView)

    val ownerNotifsModel = remember { OwnerNotifsModel() }
    val ownerNotifsController = remember { OwnerNotifsController(ownerNotifsModel) }
    val ownerNotifsView = remember { OwnerNotifsView(ownerNotifsController) }
    ownerNotifsModel.addObserver(ownerNotifsView)

    val adminImagesModel = remember { AdminImagesModel(coroutineScope) }
    val adminImagesController = remember { AdminImagesController(adminImagesModel) }
    val adminImagesView = remember { AdminImagesView(adminImagesController) }
    adminImagesModel.addObserver(adminImagesView)

    val adminReviewsModel = remember { AdminReviewsModel(coroutineScope) }
    val adminReviewsController = remember { AdminReviewsController(adminReviewsModel) }
    val adminReviewsView = remember { AdminReviewsView(adminReviewsController) }
    adminReviewsModel.addObserver(adminReviewsView)

    val adminMessagesModel = remember { AdminMessagesModel(coroutineScope) }
    val adminMessagesController = remember { AdminMessagesController(adminMessagesModel) }
    val adminMessagesView = remember { AdminMessagesView(adminMessagesController) }
    adminMessagesModel.addObserver(adminMessagesView)

    val messageUserModel = remember { MessageUserModel(coroutineScope) }
    val messageUserController = remember { MessageUserController(messageUserModel) }
    val messageUserView = remember { MessageUserView(messageUserController) }
    messageUserModel.addObserver(messageUserView)

    // Opening a QR code is an entrant thing, so we have to switch to entrant mode to open its
    LaunchedEffect(deepLinkCampsiteId) {
        if (deepLinkCampsiteId != null) {
            User.setCurrentRole(UserViewMode.ENTRANT)
            currentRole = UserViewMode.ENTRANT
            needsRoleChoice = false
        }
    }

    if (needsRoleChoice) {
        // Scaffold is just here so the screen respects the status bar / nav bar insets
        Scaffold { padding ->
            Box(Modifier.padding(padding)) {
                chooseRoleView.Render(User, onRoleChosen = { role ->
                    currentRole = role
                    needsRoleChoice = false
                })
            }
        }
    } else {
        when (currentRole) {
            UserViewMode.ENTRANT -> EntrantNavigationRoot(
                searchCampsitesScreen = searchCampsitesView,
                campsiteDetailsScreen = campsiteDetailsView,
                seeReservationsScreen = seeReservationsView,
                viewLotteriesScreen = viewLotteriesView,
                createReservationScreen = createReservationView,
                viewProfileScreen = viewProfileView,
                editProfileScreen = editProfileView,
                messageUserScreen = messageUserView,
                deepLinkCampsiteId = deepLinkCampsiteId,
                onDeepLinkHandled = onDeepLinkHandled,
                onSwitchRole = { needsRoleChoice = true })

            UserViewMode.CAMPSITE_OWNER -> CampsiteOwnerNavigationRoot(
                searchCampsitesScreen = searchCampsitesView,
                campsiteDetailsScreen = campsiteDetailsView,
                formCampsitesScreen = formCampsitesView,
                formLotteriesScreen = formLotteriesView,
                viewLotteriesScreen = viewLotteriesView,
                seeReservationsScreen = seeReservationsView,
                lotteryDetailsView = lotteryDetailsView,
                ownerNotifsView = ownerNotifsView,
                messageUserScreen = messageUserView,
                onSwitchRole = { needsRoleChoice = true })

            UserViewMode.ADMIN -> AdminNavigationRoot(
                searchCampsitesScreen = searchCampsitesView,
                campsiteDetailsScreen = campsiteDetailsView,
                adminProfilesScreen = adminProfilesView,
                adminImagesScreen = adminImagesView,
                adminReviewsScreen = adminReviewsView,
                adminMessagesScreen = adminMessagesView,
                onSwitchRole = { needsRoleChoice = true })

            null -> {
                // They have a profile but no role saved, so just send them to the entrant UI so the app is still usable
                EntrantNavigationRoot(
                    searchCampsitesScreen = searchCampsitesView,
                    campsiteDetailsScreen = campsiteDetailsView,
                    seeReservationsScreen = seeReservationsView,
                    viewLotteriesScreen = viewLotteriesView,
                    createReservationScreen = createReservationView,
                    viewProfileScreen = viewProfileView,
                    editProfileScreen = editProfileView,
                    messageUserScreen = messageUserView,
                    deepLinkCampsiteId = deepLinkCampsiteId,
                    onDeepLinkHandled = onDeepLinkHandled,
                    onSwitchRole = { needsRoleChoice = true })
            }
        }
    }
}