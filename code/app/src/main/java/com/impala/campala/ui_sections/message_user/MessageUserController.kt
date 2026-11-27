package com.impala.campala.ui_sections.message_user

import com.impala.campala.Controller

/**
 * Allows for communication between the frontend and backend of the messaging screen.
 *
 * @param model The model storing all of the data for an instance of the messaging screen.
 */
class MessageUserController(val model: MessageUserModel) : Controller {

    /**
     * Called in a LaunchedEffect in the View, loads the messages for a reservation
     *
     * @param reservationId the ID of the reservation whose messages are shown
     */
    fun loadThread(reservationId: String) {
        model.loadThread(reservationId)
    }

    /**
     * Refreshes the data for the message chain to keep it up-to-date.
     */
    fun refresh() : MessageUserModel {
        return model.refresh()
    }

    /**
     * Sends a message for the user.
     *
     * @param text The message.
     * @param owner tells whether the user is the campsite owner (true) or the entrant (false).
     */
    fun send(text: String, owner: Boolean) {
        model.send(text, owner)
    }

//    fun ownerMessages(): MutableList<MessageModel> {
//        return model.ownerMessages
//    }
//
//    fun entrantMessages(): MutableList<MessageModel> {
//        return model.entrantMessages
//    }
}