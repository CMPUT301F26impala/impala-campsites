package com.impala.campala.ui_sections.message_user

import com.impala.campala.Controller

class MessageUserController(val model: MessageUserModel) : Controller {

    /**
     * Called in a LaunchedEffect in the View, loads the messages for a reservation
     *
     * @param reservationId the ID of the reservation whose messages are shown
     */
    fun loadThread(reservationId: String) {
        model.loadThread(reservationId)
    }

    fun refresh() : MessageUserModel {
        return model.refresh()
    }

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