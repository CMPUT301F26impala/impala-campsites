package com.impala.campala.ui_sections.owner_notifs

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.NotificationModel
import kotlinx.coroutines.tasks.await

class OwnerNotifsModel(
) : ObservableModel<OwnerNotifsModel>() {
    lateinit var notifs: List<NotificationModel>

    // i dont like doing it this way but its the easiest thing for now
    lateinit var notifRecStrs: List<String>

    suspend fun load() {
        notifs =
            Firebase.firestore.collection("notifications").whereEqualTo("sender", User.getUID())
                .get().await().documents.map { NotificationModel.load(it) }
        notifRecStrs = notifs.map { it.getReceivers().map { it.name.ifBlank { "Unknown" } }.joinToString(", ") }
        notifyObservers(this)
    }
}