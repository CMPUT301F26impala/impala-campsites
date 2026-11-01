package com.impala.campala.ui_sections.choose_role

import android.util.Log
import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.UserViewMode
import com.impala.campala.model.CampsiteOwnerProfileModel
import com.impala.campala.model.CampsiteOwnerProfileRepository
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ChooseRoleModel(
    val entrantProfileRepo: EntrantProfileRepository,
    val campsiteOwnerProfileRepo: CampsiteOwnerProfileRepository,
    val scope: CoroutineScope
) : ObservableModel<ChooseRoleModel>() {
    // true while the profile is being created, so the buttons can't be pressed twice
    var isSaving = false

    // set once the role is saved, the view watches this to know when to leave the choose role screen
    var chosenRole: UserViewMode? = null

    /**
     * If this is the first time choosing said role, it will create the object for it, and then it also updates the role property on the user.
     *
     * @param user The User picking a role
     * @param role Either UserViewMode.ENTRANT or UserViewMode.CAMPSITE_OWNER
     */
    fun chooseRole(user: User, role: UserViewMode) {
        // First we set isSaving so that double presses can't happen
        isSaving = true
        notifyObservers(this)

        scope.launch {
            try {
                when (role) {
                    UserViewMode.ENTRANT -> if (!user.getHasCreatedEntrantProfile()) {
                        entrantProfileRepo.createEntrantProfile(user, EntrantProfileModel())
                    }
                    UserViewMode.CAMPSITE_OWNER -> if (!user.getHasCreatedCampsiteOwnerProfile()) {
                        campsiteOwnerProfileRepo.createCampsiteOwnerProfile(user, CampsiteOwnerProfileModel())
                    }
                    // TODO: Admin is a post-halfway checkpoint thing
                    UserViewMode.ADMIN -> throw IllegalArgumentException("Can't pick admin from the choose role screen")
                }
                user.setCurrentRole(role)
                user.updateUserProfile(user.getUID())
                chosenRole = role
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ChooseRoleModel", "Failed to save role", e)
            }
            isSaving = false
            notifyObservers(this@ChooseRoleModel)
        }
    }
}
