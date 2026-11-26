package com.impala.campala.ui_sections.admin_messages

import com.impala.campala.Controller

/**
 * Controller for the admin's messages tab
 *
 * @param model the AdminMessagesModel
 */
class AdminMessagesController(val model: AdminMessagesModel) : Controller {
    fun initialize() {
        model.initialize()
    }

    /**
     * Called when the delete button is pressed
     *
     * @param adminMessage the message being deleted
     */
    fun deleteMessage(adminMessage: AdminMessage) {
        model.deleteMessage(adminMessage)
    }
}
