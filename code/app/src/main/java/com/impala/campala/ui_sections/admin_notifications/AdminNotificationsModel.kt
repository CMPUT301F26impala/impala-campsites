package com.impala.campala.ui_sections.admin_notifs

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.impala.campala.ObservableModel
import com.impala.campala.model.NotificationModel
import com.impala.campala.model.EntrantProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Model for the notifications branch of admin UI
 *
 * @param scope the Coroutine Scope
 *
 * @property everyRetrievedNotification array of all notifications pulled from firebase
 * @property isNowFetchingEveryStoredNotification if the array of notifications is still being pulled
 * @property didRetrievalOfEveryNotificationFail if fetching array notifications failed
 * @property everyNotificationParticipantNameById map of names of senders and receivers using their user IDs
 */
class AdminNotificationsModel(val scope: CoroutineScope) : ObservableModel<AdminNotificationsModel>() {

    var everyRetrievedNotification: List<NotificationModel> = emptyList()
    var isNowFetchingEveryStoredNotification = false
    var didRetrievalOfEveryNotificationFail = false
    var everyNotificationParticipantNameById: Map<String, String> = emptyMap()

    /**
     * Gets all stored notifications from the database
     */
    fun retrieveEveryStoredNotification() {
        if (isNowFetchingEveryStoredNotification) { return }
        isNowFetchingEveryStoredNotification = true
        didRetrievalOfEveryNotificationFail = false
        notifyObservers(this)

        scope.launch {
            try {
                val everyFetchedNotificationDocument =
                    Firebase.firestore.collection("notifications").get().await().documents
                everyRetrievedNotification = everyFetchedNotificationDocument.map { currentNotificationDocument ->
                    NotificationModel.load(currentNotificationDocument)
                }
                retrieveNotificationParticipantNames()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                didRetrievalOfEveryNotificationFail = true
                Log.e("AdminNotifsModel", "Failed to get notifications", e)
            } finally {
                isNowFetchingEveryStoredNotification = false
                notifyObservers(this@AdminNotificationsModel)
            }
        }
    } // end of retrieve function

    /**
     * Get the names of all senders and receivers
     */
    suspend fun retrieveNotificationParticipantNames() {
        val everyRetrievedParticipantName = mutableMapOf<String, String>()
        val everyUniqueNotificationParticipantId = mutableSetOf<String>()
        for (currentStoredNotification in everyRetrievedNotification) {
            everyUniqueNotificationParticipantId.add(currentStoredNotification.sender)
            everyUniqueNotificationParticipantId.addAll(currentStoredNotification.receivers)
        }
        for (currentParticipantId in everyUniqueNotificationParticipantId) {
            everyRetrievedParticipantName[currentParticipantId] = retrieveParticipantNameById(currentParticipantId)
        }
        everyNotificationParticipantNameById = everyRetrievedParticipantName
    }

    /**
     * Uses user ID and gets the name of the user
     *
     * @param participantUserId user ID
     *
     * @return name of the user. ID itself if its blank
     */
    suspend fun retrieveParticipantNameById(participantUserId: String): String {
        if (participantUserId.isBlank()) { return "Unknown" }
        if (participantUserId == "SYSTEM") { return "System" }
        val retrievedParticipantEntrantProfile = EntrantProfileRepository.getEntrantProfileById(participantUserId)
        if (retrievedParticipantEntrantProfile != null) { if (retrievedParticipantEntrantProfile.name.isNotBlank()) { return retrievedParticipantEntrantProfile.name } }
        val retrievedParticipantUserDocument = Firebase.firestore.collection("users").document(participantUserId).get().await()
        val retrievedParticipantUserName = retrievedParticipantUserDocument.getString("userName")
        if (retrievedParticipantUserName != null) { if (retrievedParticipantUserName.isNotBlank()) { return retrievedParticipantUserName } }
        return participantUserId
    }
}