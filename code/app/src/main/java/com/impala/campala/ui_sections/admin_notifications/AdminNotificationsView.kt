package com.impala.campala.ui_sections.admin_notifs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.impala.campala.ViewObserver
import com.impala.campala.model.NotificationModel
import com.impala.campala.ui.theme.AppTheme
import com.impala.campala.ui_sections.components.CardDetail

/**
 * View for the notifications branch of admin UI
 *
 * @param controller the admin notification controller
 */
class AdminNotificationsView(val controller: AdminNotificationsController) : ViewObserver<AdminNotificationsModel> {
    var everyRetrievedNotification by mutableStateOf<List<NotificationModel>>(emptyList())
    var isNowFetchingEveryStoredNotification by mutableStateOf(false)
    var didRetrievalOfEveryNotificationFail by mutableStateOf(false)
    var everyNotificationParticipantNameById by mutableStateOf<Map<String, String>>(emptyMap())

    /**
     * Update details to the ui front end
     *
     * @param model the model containing the required details
     */
    override fun update(model: AdminNotificationsModel) {
        everyRetrievedNotification = model.everyRetrievedNotification
        isNowFetchingEveryStoredNotification = model.isNowFetchingEveryStoredNotification
        didRetrievalOfEveryNotificationFail = model.didRetrievalOfEveryNotificationFail
        everyNotificationParticipantNameById = model.everyNotificationParticipantNameById
    }
    init { update(controller.model) }

    /**
     * Render the actual notifications to the screen
     */
    @Composable
    fun Render() {
        if (!LocalInspectionMode.current) {
            LaunchedEffect(Unit) { controller.onFirstRender() }
        }
        if (isNowFetchingEveryStoredNotification) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (didRetrievalOfEveryNotificationFail) {
            Text("Failed to load notifications!!!")
        } else if (everyRetrievedNotification.isEmpty()) {
            Text("No notifications found!")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) { items(everyRetrievedNotification) { currentStoredNotification -> StyleIndividualNotificationDetails(currentStoredNotification) } }
        }
    } // end of render for prsnl ref

    /**
     * Display details of the notifications itself
     *
     * @param currentStoredNotification the notification to display
     */
    @Composable
    private fun StyleIndividualNotificationDetails(currentStoredNotification: NotificationModel) {
        val currentNotificationSenderName = findParticipantDisplayName(currentStoredNotification.sender)
        val everyNotificationReceiverName = mutableListOf<String>()
        for (currentReceiverId in currentStoredNotification.receivers) { everyNotificationReceiverName.add(findParticipantDisplayName(currentReceiverId)) }
        var combinedNotificationReceiverNames = everyNotificationReceiverName.joinToString(", ")
        if (combinedNotificationReceiverNames.isBlank()) { combinedNotificationReceiverNames = "None" }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(currentStoredNotification.title, style = MaterialTheme.typography.headlineSmall)
            CardDetail(Icons.Default.Person, "From: $currentNotificationSenderName")
            CardDetail(Icons.Default.AccountBox, "To: $combinedNotificationReceiverNames")
            if (currentStoredNotification.sent) {
                CardDetail(Icons.Default.Done, "Sent")
            } else {
                CardDetail(Icons.Default.Info, "Processing")
            }
            Text(currentStoredNotification.body, style = MaterialTheme.typography.bodyLarge)
        }
    }

    /**
     * Gets the name of the participant
     *
     * @param participantUserId user id
     *
     * @return name of the user. id if it doesn't exist
     */
    private fun findParticipantDisplayName(participantUserId: String): String {
        val retrievedParticipantName = everyNotificationParticipantNameById[participantUserId]
        if (retrievedParticipantName != null) { return retrievedParticipantName }
        if (participantUserId.isBlank()) { return "Unknown" }
        return participantUserId
    }
}

/**
 * Preview of the admin notifications screen using dummy data
 */
@Preview(showBackground = true, widthDp = 393, heightDp = 800)
@Composable
fun AdminNotificationsViewPreview() {
    val scope = rememberCoroutineScope()
    val model = remember(scope) {
        val previewModel = AdminNotificationsModel(scope)

        previewModel.everyRetrievedNotification = listOf(
            NotificationModel(
                title = "New Notification From Camp",
                body = "Hey! Just letting you know that when you get there, the key is under the doormat.",
                sender = "owner1",
                receivers = mutableListOf("entrant1"),
                sent = true
            ),
            NotificationModel(
                title = "New Notification From Lottery",
                body = "Hello all waitlist entrants, unfortunately a storm destroyed our 2nd cabin and so with the reduced capacity it's become even harder to get a spot.",
                sender = "owner2",
                receivers = mutableListOf("entrant1", "entrant2", "entrant3"),
                sent = true
            ),
            NotificationModel(
                title = "You Won A Lottery",
                body = "Reserve dates in the 'Lottery' tab on the home screen.",
                sender = "SYSTEM",
                receivers = mutableListOf("entrant2"),
                sent = true
            ),
            NotificationModel(
                title = "You Lost A Lottery",
                body = "Better Luck Next Time",
                sender = "SYSTEM",
                receivers = mutableListOf("entrant3"),
                sent = false
            )
        )

        previewModel.everyNotificationParticipantNameById = mapOf(
            "owner1" to "John Smith",
            "owner2" to "Michael Wilson",
            "entrant1" to "Alex Johnson",
            "entrant2" to "Sarah Williams",
            "entrant3" to "David Brown",
            "SYSTEM" to "System"
        )
        previewModel
    }

    val controller = remember(model) { AdminNotificationsController(model) }
    val view = remember(controller) { AdminNotificationsView(controller) }

    AppTheme() {
        Scaffold { padding ->
            Box(Modifier.padding(padding).padding(horizontal = 16.dp)) {
                view.Render()
            }
        }
    }
}