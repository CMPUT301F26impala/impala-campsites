package com.impala.campala.ui_sections.admin_notifs

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.impala.campala.ObservableModel
import com.impala.campala.model.NotificationModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Model for the notifications branch of admin UI
 *
 * @param scope the Coroutine Scope
 *
 * @property allRetrievedNotifications array of all notifications pulled from firebase
 * @property isFetchingStoredNotifications if the array of notifications is still being pulled
 * @property wasNotificationRetrievalUnsuccessful if fetching array notifications failed
 */
class AdminNotificationsModel(val scope: CoroutineScope) : ObservableModel<AdminNotificationsModel>() {

    var allRetrievedNotifications: List<NotificationModel> = emptyList()
    var isFetchingStoredNotifications = false
    var wasNotificationRetrievalUnsuccessful = false

    /**
     * Retrieves all notifications stored in the database regardless
     * of who sent or received them.
     * Notifies the view before and after retrieving the notifications.
     */
    fun retrieveAllStoredNotifications() {
        if (isFetchingStoredNotifications) { return }
        isFetchingStoredNotifications = true
        wasNotificationRetrievalUnsuccessful = false
        notifyObservers(this)

        scope.launch {
            try {
                val fetchedNotificationDocuments =
                    Firebase.firestore.collection("notifications").get().await().documents
                allRetrievedNotifications = fetchedNotificationDocuments.map { currentNotificationDocument ->
                    NotificationModel.load(currentNotificationDocument)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                wasNotificationRetrievalUnsuccessful = true
                Log.e("AdminNotifsModel", "Failed to get notifications", e)
            } finally {
                isFetchingStoredNotifications = false
                notifyObservers(this@AdminNotificationsModel)
            }
        }
    } // end of retrieve function
}