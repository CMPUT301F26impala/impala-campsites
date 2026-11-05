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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip
import com.impala.campala.UserViewMode
import com.impala.campala.ViewObserver
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.EntrantProfileModel
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
 * On the campsite owner screen it instead displays the active, past, and cancelled reservations at their campsites
 *
 * @param controller the SeeReservationsController that this view uses
 */
class SeeReservationsView(val controller: SeeReservationsController) : ViewObserver<SeeReservationsModel> {
    private var campsites by mutableStateOf<List<CampsiteModel>>(emptyList())
    private var entrants by mutableStateOf<List<EntrantProfileModel>>(emptyList())
    private var oldReservations by mutableStateOf<List<ReservationModel>>(emptyList())
    private var newReservations by mutableStateOf<List<ReservationModel>>(emptyList())
    private var cancelledReservations by mutableStateOf<List<ReservationModel>>(emptyList())
    private var viewMode by mutableStateOf(UserViewMode.ENTRANT)

    override fun update(model: SeeReservationsModel) {
        this.campsites = model.campsites
        this.entrants = model.entrants
        this.oldReservations = model.oldReservations
        this.newReservations = model.newReservations
        this.cancelledReservations = model.cancelledReservations
        this.viewMode = model.viewMode
    }

    // SCREEN_A = Scheduled/Upcoming Reservations, SCREEN_B = Past, SCREEN_C = Past with the rate modal on top of it, as per how Liam designed this
    private enum class UiTarget { SCREEN_A, SCREEN_B, SCREEN_C }

    @Composable
    fun Render(navController: NavController) {
        // Load the user's reservations every time this tab is shown, so new bookings show up
        LaunchedEffect(Unit) {
            controller.initialize()
        }

        // To preserve Liam's design of the A, B, C screens, I've made Owner just it's own thing
        if (viewMode == UserViewMode.CAMPSITE_OWNER) {
            DrawOwnerRes()
            return
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
     * The campsite owner's version of this tab, with Active, Past, and Cancelled tabs
     */
    @Composable
    private fun DrawOwnerRes() {
        // Instead of doing it based off of an enum, I'm just using the index of the tab in the tab bar
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        // This is so that the modal knows what's selected
        var reservationBeingRejected by remember { mutableStateOf<ReservationModel?>(null) }
        val tabNames = listOf("Active", "Past", "Cancelled")

        Column() {
            PrimaryTabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier) {
                tabNames.forEachIndexed { index, name ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = name,
                                maxLines = 1,
                            )
                        }
                    )
                }
            }

            // This just renders one lazyColumn and switched the content inside of it instead of having three lazy columns
            val shownReservations = when (selectedTabIndex) {
                0 -> newReservations
                1 -> oldReservations
                else -> cancelledReservations
            }
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(shownReservations) { res ->
                    Spacer(modifier = Modifier.height(16.dp))
                    DrawOwnerResRow(res, showActions = selectedTabIndex == 0, onReject = { reservationBeingRejected = res })
                }
            }
        }

        reservationBeingRejected?.let { res ->
            DrawRejectModal(res,
                onClose = { reservationBeingRejected = null },
                onConfirm = { controller.rejectReservation(res) }
            )
        }
    }

    /**
     * One reservation on the campsite owner's screen, with relevant info and actions
     *
     * @param res the reservation
     * @param showActions whether to show the Reject and Message buttons, only true on Active
     * @param onReject called when the Reject button is pressed
     */
    @Composable
    private fun DrawOwnerResRow(res: ReservationModel, showActions: Boolean, onReject: () -> Unit) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // TODO: Profile pictures
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = getEntrantName(res),
                        fontSize = 24.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            // The little bar at the end acts like a divider div
                            text = "${campsites.find { it.id == res.campsiteId }?.name ?: "Unknown Campsite"} |",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DrawDateRange(res)
                    }
                    // It's impossible to make a reservation without accepting the rules, so this is always true
                    // We need to show this to fulfill the user story for it though, because the campsite owner has to know it happened
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Acknowledged rules",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (showActions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = MaterialTheme.colorScheme.onTertiary),
                        onClick = onReject) {
                        Text("Reject")
                    }
                    // Messaging is a final checkpoint thing
                    Button(modifier = Modifier.weight(1f),
                        enabled = false,
                        onClick = {
                            //go to messages
                        }) {
                        Text("Message")
                    }
                }
            }
        }
    }

    /**
     * The name of whoever made the reservation, entrant profiles don't always have a name set yet so this has a fallback
     *
     * @param res the reservation
     */
    private fun getEntrantName(res: ReservationModel): String {
        val name = entrants.find { it.userId == res.entrantId }?.name
        return if (name.isNullOrBlank()) "Unknown Entrant" else name
    }

    /**
     * The modal for a campsite owner to reject an active reservation
     *
     * @param res the active reservation being rejected
     * @param onClose called when the modal is closed, whether by Cancel, Reject, or tapping outside it
     * @param onConfirm called when the rejection has been confirmed
     */
    @Composable
    private fun DrawRejectModal(res: ReservationModel, onClose: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text("Reject reservation?") },
            text = {
                Text("${getEntrantName(res)}'s reservation at ${campsites.find { it.id == res.campsiteId }?.name ?: "this campsite"} will be cancelled. This can't be undone.")
            },
            dismissButton = {
                TextButton(onClick = onClose) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onClose()
                }) {
                    Text("Reject")
                }
            }
        )
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
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d"))
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
