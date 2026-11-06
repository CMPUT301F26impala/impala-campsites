package com.impala.campala.ui_sections.choose_role

import com.impala.campala.Controller
import com.impala.campala.User
import com.impala.campala.UserViewMode

/**
 * The controller for the ChooseRoleView
 *
 * @param model the model which owns the view that this class will update
 */
class ChooseRoleController(val model: ChooseRoleModel) : Controller {
    /**
     * Fire when the user selects entrant as their role
     *
     * @param user The user's object
     */
    fun useAsEntrant(user: User) {
        model.chooseRole(user, UserViewMode.ENTRANT)
    }

    /**
     * Fire when the user selects campsite owner as their role
     *
     * @param user The user's object
     */
    fun useAsCampsiteOwner(user: User) {
        model.chooseRole(user, UserViewMode.CAMPSITE_OWNER)
    }

    /**
     * Fires when the user's role changes
     */
    fun onRoleChosenHandled() {
        model.clearChosenRole()
    }
}
