package com.impala.campala.ui_sections.admin_messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.impala.campala.ViewObserver
import com.impala.campala.ui_sections.components.CardDetail

/**
 * View for the admin's messages tab
 *
 * @param controller the AdminMessagesController for this View
 */
class AdminMessagesView(val controller: AdminMessagesController) : ViewObserver<AdminMessagesModel> {
    var messages by mutableStateOf<List<AdminMessage>>(emptyList())
    var campsiteNames by mutableStateOf<Map<String, String>>(emptyMap())

    private var pendingDelete by mutableStateOf<AdminMessage?>(null)

    override fun update(model: AdminMessagesModel) {
        messages = model.messages
        campsiteNames = model.campsiteNames
    }

    @Composable
    fun Render() {
        LaunchedEffect(Unit) {
            controller.initialize()
        }

        if (messages.isEmpty()) {
            Text("No messages yet", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { adminMessage ->
                    RenderMessage(adminMessage)
                }
            }
        }

        pendingDelete?.let { adminMessage ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text("Delete message?") },
                text = { Text("This message will be permanently removed.") },
                confirmButton = {
                    TextButton(onClick = {
                        controller.deleteMessage(adminMessage)
                        pendingDelete = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
                }
            )
        }
    }

    @Composable
    private fun RenderMessage(adminMessage: AdminMessage) {
        val message = adminMessage.message
        val date = message.localDate()
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    campsiteNames[adminMessage.thread.reservationId] ?: "Unknown campsite",
                    style = MaterialTheme.typography.headlineSmall
                )
                CardDetail(Icons.Default.Person, if (message.fromOwner) "Owner" else "Entrant")
                CardDetail(Icons.Default.DateRange, "%s %02d:%02d".format(date.date, date.hour, date.minute))
                Text(message.body, style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = { pendingDelete = adminMessage }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete message",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
