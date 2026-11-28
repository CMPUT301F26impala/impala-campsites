package com.impala.campala.ui_sections.admin_notifs

import com.impala.campala.Controller

/**
 * Controller for the notifications branch of admin UI
 *
 * @param model the admin notifications model
 */
class AdminNotificationsController(val model: AdminNotificationsModel) : Controller {
    /**
     * get all notifications on first open of the screen
     */
    fun onFirstRender() { model.retrieveEveryStoredNotification() }
}