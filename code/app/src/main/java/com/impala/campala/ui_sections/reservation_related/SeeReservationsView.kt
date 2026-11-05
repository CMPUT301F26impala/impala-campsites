package com.impala.campala.ui_sections.reservation_related

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.impala.campala.ViewObserver
import com.impala.campala.model.ReservationModel
import com.impala.campala.model.ReservationRepository
import com.google.firebase.Timestamp
import java.time.ZoneId
import com.impala.campala.ui.theme.tertiaryContainerDark
import com.impala.campala.ui.theme.tertiaryContainerLight
import com.impala.campala.ui.theme.tertiaryDark
import com.impala.campala.ui.theme.tertiaryLight
import java.time.format.DateTimeFormatter

class SeeReservationsView(val controller: SeeReservationsController) {

    private enum class UiTarget { SCREEN_A, SCREEN_B }

    @Composable
    fun Render(navController: NavController) {
        // Load the user's reservations every time this tab is shown, so new bookings show up
        LaunchedEffect(Unit) {
            controller.initialize(controller.user.getUID())
        }

        var selectedTabIndex by remember { mutableIntStateOf(0) }
        var currentScreen by remember {mutableStateOf(UiTarget.SCREEN_A)}

        Column() {

            PrimaryTabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        selectedTabIndex = 0
                        currentScreen = UiTarget.SCREEN_A
                        //TODO The first shit
                    },
                    text = {
                        Text(
                            text = "Scheduled",
                            maxLines = 1,
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        selectedTabIndex = 1
                        currentScreen = UiTarget.SCREEN_B
                        //TODO the second shit
                    },
                    text = {
                        Text(
                            text = "Past",
                            maxLines = 1,
                        )
                    }
                )
            }

            // Simple way to allow for a "composable function pointer" to point to and call two different composable functions.
            // Demonstrated by Gemini Flash-lite 3.5 with prompt "how to have a mutable state function that can be one of two composable UI functions in kotlin".
            // This would take zero (0) lines and zero (0) google searches to accomplish in C. Just saying. But OOP is super useful or something trust me
            when (currentScreen) {
                UiTarget.SCREEN_A -> CurrentRes()
                UiTarget.SCREEN_B -> PrevRes()
            }
        }
    }

    @Composable
    private fun CurrentRes() {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(controller.newReservations) { res ->
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = controller.campsites.find { it.id == res.campsiteId }?.name ?: "Unknown Campsite",
                    fontSize = 24.sp
                )
                ReservationDates(res)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Messaging is a final checkpoint thing
                    Button(modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false,
                        onClick = {
                            //go to messages
                        }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Messages",
                        )
                    }

                    Button(modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tertiaryContainerDark, contentColor = tertiaryDark),
                        onClick = {
                            //cancel reservation
                        }) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cancel Reservation",
                            fontSize = 12.sp,
                            color = tertiaryDark
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun PrevRes() {
        LazyColumn(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            items(controller.oldReservations) { res ->
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = controller.campsites.find { it.id == res.campsiteId }?.name ?: "Unknown Campsite",
                    fontSize = 24.sp
                )
                DrawDateRange(res)

                Button(modifier = Modifier,
                    onClick = {
                        //currentScreen = UiTarget.SCREEN_C // TODO: SCREEN_C should be the rating screen
                    }) {
                    Text(
                        text = "Rate this campsite",
                        fontSize = 24.sp
                    )
                }
            }
        }
    }

    /**
     * Draws a date icon and the date range of the stay, extracted here for readability
     *
     * @param res the reservation
     */
    @Composable
    private fun DrawDateRange(res: ReservationModel) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "${formatDate(res.startDate)} - ${formatDate(res.endDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    private fun formatDate(timestamp: Timestamp): String {
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, uuuu"))
    }
}
