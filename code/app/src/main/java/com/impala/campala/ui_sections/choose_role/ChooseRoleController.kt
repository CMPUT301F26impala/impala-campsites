package com.impala.campala.ui_sections.choose_role

import com.impala.campala.Controller
import com.impala.campala.User
import com.impala.campala.UserViewMode

class ChooseRoleController(val model: ChooseRoleModel) : Controller {
    fun useAsEntrant(user: User) {
        model.chooseRole(user, UserViewMode.ENTRANT)
    }

    fun useAsCampsiteOwner(user: User) {
        model.chooseRole(user, UserViewMode.CAMPSITE_OWNER)
    }

    fun onRoleChosenHandled() {
        model.clearChosenRole()
    }
}
