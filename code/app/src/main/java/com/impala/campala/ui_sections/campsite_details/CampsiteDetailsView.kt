package com.impala.campala.ui_sections.campsite_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.impala.campala.ViewObserver
import com.google.firebase.Timestamp
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.ReservationModel
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * The Campsite Details Screen
 * This takes several flags and the buttons that are displayed vary depending on which are active
 *
 * @param controller the CampsiteDetailsController instance that is connected to the model
 */
class CampsiteDetailsView(val controller: CampsiteDetailsController): ViewObserver<CampsiteDetailsModel> {
    private var isOwner by mutableStateOf(false)
    private var lotteryExists by mutableStateOf(false)
    private var name by mutableStateOf("")
    private var location by mutableStateOf("")
    private var description by mutableStateOf("")
    private var maxOccupancy by mutableIntStateOf(0)
    private var averageRating by mutableStateOf<Double?>(null)
    private var tags = mutableStateListOf<String>()
    private var joinStatus by mutableStateOf<LotteryModel.ProspectiveJoinStatus?>(null)
    private var reservedDates = mutableStateListOf<ReservationModel>()

    override fun update(model: CampsiteDetailsModel) {
        this.name = model.name
        this.location = model.location
        this.description = model.description
        this.maxOccupancy = model.maxOccupancy
        this.averageRating = model.averageRating
        this.tags.clear()
        this.tags.addAll(model.tags)
        this.isOwner = model.userIsOwner
        this.lotteryExists = model.lotteryExists
        this.joinStatus = model.joinStatus
        this.reservedDates.clear()
        this.reservedDates.addAll(model.reservedDates)
    }

    /**
     * The main render function to output the screen
     *
     * Also asks the controller to load the campsite whenever the screen is shown with a new id
     *
     * @param navController the root NavController, used to go back to the previous screen
     * @param campsiteId the id of the campsite to show
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    fun Render(navController: NavController, campsiteId: String) {
        LaunchedEffect(campsiteId) {
            controller.onOpen(campsiteId)
        }
        Column(modifier = Modifier.fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
                    .height(192.dp)
            ) {}
            Row(
                modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(name, style = MaterialTheme.typography.headlineMedium)
                if(isOwner) {
                    IconButton(
                        onClick = { controller.moveToEdit(navController, campsiteId) }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Icon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            RenderIconText()
            RenderTextSection("Description", description)
            RenderTextSection("Tags", tags.joinToString())
            if(isOwner) {
                // TODO: This is not how our storyboard describes this should look, this will need to be polished up in final checkpoint to actually use the calendar renderer
                // But for now, I had Claude Opus 5.5 generate this .joinToString call, with the prompt "How can I turn ranges of dates into a range string but still have each range be it's own line"
                RenderTextSection(
                    "Reserved Dates",
                    reservedDates.joinToString("\n") { "${formatDate(it.startDate)} - ${formatDate(it.endDate)}" }
                        .ifEmpty { "No upcoming reservations" }
                )
            }
            RenderBottomButtons(modifier = Modifier.weight(1f), navController)
        }
    }

    /**
     * The function to render details as short lines of text beside icons
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    private fun RenderIconText() {
        Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = "Location Icon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = "Person Icon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Up to $maxOccupancy occupants", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = "Star Icon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                averageRating?.let { "%.1f Stars".format(it) } ?: "No ratings yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    /**
     * A function to render a section of text with a larger title and a block of text underneath
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    private fun RenderTextSection(title: String, content: String) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 10.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 10.dp))
    }

    private fun formatDate(timestamp: Timestamp): String {
        return timestamp.toInstant().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }

    /**
     * A function to render the relevant buttons in the bottom right of the screen based on flags
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    private fun RenderBottomButtons(modifier: Modifier = Modifier, navController: NavController) {
        Column(modifier = modifier.padding(all = 10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            if(isOwner) {
                Button(onClick = { controller.generateQRCode() }) { Text("Generate QR") }
            }
            // Show a different button based on whether the lottery exists and is full
            else {
                var buttonText: String
                var enabled = true
                var buttonAction: () -> Unit = {}

                if(!lotteryExists || joinStatus == LotteryModel.ProspectiveJoinStatus.FULL) {
                    buttonText = "Lottery Closed"
                    enabled = false
                }
                else if(joinStatus == LotteryModel.ProspectiveJoinStatus.ALREADY_IN_LOTTERY) {
                    buttonText = "Already In Lottery"
                    enabled = false
                }
                else if(joinStatus == LotteryModel.ProspectiveJoinStatus.CAN_JOIN_LOTTERY) {
                    buttonText = "Join Lottery"
                    buttonAction = { controller.joinLottery() }
                }
                else if(joinStatus == LotteryModel.ProspectiveJoinStatus.ALREADY_IN_WAITLIST) {
                    buttonText = "Leave Waitlist"
                    buttonAction = { controller.leaveWaitlist() }
                }
                else {
                    buttonText = "Join Waitlist"
                    buttonAction = { controller.joinLottery() }
                }
                Button(
                    onClick = buttonAction,
                    enabled = enabled) {
                    Text(buttonText) }
            }
            // This button will move the user back to the Browse Campsite screen
            IconButton(onClick = { controller.moveToBrowse(navController) }) {
                Icon(Icons.Default.Close, contentDescription = "Back Icon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}