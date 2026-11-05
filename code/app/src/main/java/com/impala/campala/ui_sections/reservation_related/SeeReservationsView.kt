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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
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
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.ReservationModel
import com.impala.campala.model.ReservationRepository
import com.google.firebase.Timestamp
import java.time.ZoneId
import com.impala.campala.ui.theme.tertiaryContainerDark
import com.impala.campala.ui.theme.tertiaryContainerLight
import com.impala.campala.ui.theme.tertiaryDark
import com.impala.campala.ui.theme.tertiaryLight
import java.time.format.DateTimeFormatter

/**
 * The view for the 'reservations' tab on the entrant screen
 * It displays a users current and past reservations, with handling for the cancel action and the rate action
 *
 * @param controller the SeeReservationsController that this view uses
 */
class SeeReservationsView(val controller: SeeReservationsController) : ViewObserver<SeeReservationsModel> {
    private var campsites by mutableStateOf<List<CampsiteModel>>(emptyList())
    private var oldReservations by mutableStateOf<List<ReservationModel>>(emptyList())
    private var newReservations by mutableStateOf<List<ReservationModel>>(emptyList())

    override fun update(model: SeeReservationsModel) {
        this.campsites = model.campsites
        this.oldReservations = model.oldReservations
        this.newReservations = model.newReservations
    }

    // SCREEN_A = Scheduled/Upcoming Reservations, SCREEN_B = Past, SCREEN_C = Past with the rate modal on top of it, as per how Liam designed this
    private enum class UiTarget { SCREEN_A, SCREEN_B, SCREEN_C }

    @Composable
    fun Render(navController: NavController) {
        // Load the user's reservations every time this tab is shown, so new bookings show up
        LaunchedEffect(Unit) {
            controller.initialize()
        }

        var selectedTabIndex by remember { mutableIntStateOf(0) }
        var currentScreen by remember {mutableStateOf(UiTarget.SCREEN_A)}
        // This is so that the modal knows WHICH reservation is being rated
        var reservationBeingRated by remember { mutableStateOf<ReservationModel?>(null) }

        Column() {
            PrimaryTabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        selectedTabIndex = 0
                        currentScreen = UiTarget.SCREEN_A
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
                UiTarget.SCREEN_B -> PrevRes(onRate = { res ->
                    reservationBeingRated = res
                    currentScreen = UiTarget.SCREEN_C
                })
                UiTarget.SCREEN_C -> {
                    PrevRes(onRate = {})
                    reservationBeingRated?.let { res ->
                        DrawRateModal(res,
                            onClose = {
                                reservationBeingRated = null
                                currentScreen = UiTarget.SCREEN_B
                            },
                            onConfirm = { rating ->
                                controller.rateReservation(res, rating)
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CurrentRes() {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(newReservations) { res ->
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = campsites.find { it.id == res.campsiteId }?.name ?: "Unknown Campsite",
                    fontSize = 24.sp
                )
                DrawDateRange(res)

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
    private fun PrevRes(onRate: (ReservationModel) -> Unit) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(oldReservations) { res ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = campsites.find { it.id == res.campsiteId }?.name ?: "Unknown Campsite",
                            fontSize = 24.sp
                        )
                        DrawDateRange(res)
                    }

                    // If the res has already been rated, we don't need the rate button
                    if (!res.hasRating) {
                        Button(onClick = { onRate(res) }) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rate")
                        }
                    }
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

    /**
     * The rate campsite modal, otherwise known as SCREEN_C
     *
     * @param res the past reservation being rated
     * @param onClose called when the modal is closed, whether by Cancel, Confirm, or tapping outside it
     * @param onConfirm called when a confirmation has happened, and given the chosen rating
     */
    @Composable
    private fun DrawRateModal(res: ReservationModel, onClose: () -> Unit, onConfirm: (Double) -> Unit) {
        // Starts in the middle each time the modal opens
        var rating by remember { mutableFloatStateOf(3f) }
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text("Rate ${campsites.find { it.id == res.campsiteId }?.name ?: ""}") },
            text = {
                Column {
                    Slider(
                        value = rating,
                        onValueChange = { rating = it },
                        valueRange = 1f..5f,
                        // 7 steps is 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 1 and 5 are already included as the start and end
                        steps = 7
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("1", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("5", style = MaterialTheme.typography.titleLarge)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onClose) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(rating.toDouble())
                    onClose()
                }) {
                    Text("Confirm")
                }
            }
        )
    }
}
