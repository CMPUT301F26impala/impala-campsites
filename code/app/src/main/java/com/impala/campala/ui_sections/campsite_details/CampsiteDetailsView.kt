package com.impala.campala.ui_sections.campsite_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.impala.campala.ImageDisplay
import com.impala.campala.ViewObserver
import com.google.firebase.Timestamp
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.ReservationModel
import com.impala.campala.model.ReviewModel
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
    private var isAdmin by mutableStateOf(false)
    private var lotteryExists by mutableStateOf(false)
    private var name by mutableStateOf("")
    private var location by mutableStateOf("")
    private var description by mutableStateOf("")
    private var maxOccupancy by mutableIntStateOf(0)
    private var averageRating by mutableStateOf<Double?>(null)
    private var imageBytes by mutableStateOf<ByteArray?>(null)
    private var tags = mutableStateListOf<String>()
    private var joinStatus by mutableStateOf<LotteryModel.ProspectiveJoinStatus?>(null)
    private var reservedDates = mutableStateListOf<ReservationModel>()
    private var reviews = mutableStateListOf<ReviewModel>()

    override fun update(model: CampsiteDetailsModel) {
        this.name = model.name
        this.location = model.location
        this.description = model.description
        this.maxOccupancy = model.maxOccupancy
        this.averageRating = model.averageRating
        this.imageBytes = model.imageBytes
        this.tags.clear()
        this.tags.addAll(model.tags)
        this.isOwner = model.userIsOwner
        this.isAdmin = model.userIsAdmin
        this.lotteryExists = model.lotteryExists
        this.joinStatus = model.joinStatus
        this.reservedDates.clear()
        this.reservedDates.addAll(model.reservedDates)
        this.reviews.clear()
        this.reviews.addAll(model.reviews)
    }

    /**
     * The main render function to output the screen
     *
     * Also asks the controller to load the campsite whenever the screen is shown with a new id
     *
     * @param navController the root NavController, used to go back to the previous screen
     * @param campsiteId the id of the campsite to show
     *
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    fun Render(navController: NavController, campsiteId: String) {
        LaunchedEffect(campsiteId) {
            controller.onOpen(campsiteId)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                ImageDisplay(
                    imageBytes = imageBytes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(192.dp)
                )
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
                RenderReviews()
                // Room for the floating buttons
                Spacer(modifier = Modifier.height(128.dp))
            }
            RenderBottomButtons(modifier = Modifier.align(Alignment.BottomEnd), navController)
        }
    }

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

    @Composable
    private fun RenderTextSection(title: String, content: String) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 10.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 10.dp))
    }

    @Composable
    private fun RenderReviews() {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Reviews", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 10.dp))
        if (reviews.isEmpty()) {
            Text("No reviews yet", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 10.dp))
        }
        reviews.forEach { review ->
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("%.1f Stars".format(review.rating), style = MaterialTheme.typography.titleMedium)
                }
                if (review.reviewText.isNotBlank()) {
                    Text(review.reviewText, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    private fun formatDate(timestamp: Timestamp): String {
        return timestamp.toInstant().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }


    // A function to render the relevant buttons in the bottom right of the screen based on flags
    @Composable
    private fun RenderBottomButtons(modifier: Modifier = Modifier, navController: NavController) {
        Column(modifier = modifier.padding(all = 10.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            if(isAdmin) {
                RenderAdminButtons()
            }
            else if(isOwner) {
                Button(onClick = { controller.generateQRCode() }) { Text("Generate QR") }
            }
            // Show a different button based on whether the lottery exists and is full
            else {
                var buttonText: String
                var enabled = true
                var buttonAction: () -> Unit = {}

                if(!lotteryExists || joinStatus == LotteryModel.ProspectiveJoinStatus.FULL || joinStatus == LotteryModel.ProspectiveJoinStatus.NOT_OPEN) {
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

    // The admin's moderation buttons, each one asks for confirmation in a dialog before doing anything
    @Composable
    private fun RenderAdminButtons() {
        var banDialogOpen by remember { mutableStateOf(false) }
        var deleteDialogOpen by remember { mutableStateOf(false) }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { banDialogOpen = true },
                // Smaller side padding than default so both buttons fit side by side on a phone
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ban Campsite Owner")
            }
            Button(
                onClick = { deleteDialogOpen = true },
                // Smaller side padding than default so both buttons fit side by side on a phone
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Campsite")
            }
        }

        if (banDialogOpen) {
            AlertDialog(
                onDismissRequest = { banDialogOpen = false },
                title = { Text("Confirm Campsite Owner Profile Deletion") },
                text = { Text("Are you sure you want to ban the profile that owns this campsite? All of their campsites will be deleted and this cannot be undone.") },
                dismissButton = {
                    TextButton(onClick = { banDialogOpen = false }) { Text("Go Back") }
                },
                confirmButton = {
                    TextButton(onClick = {
                        banDialogOpen = false
                        controller.banOwner()
                    }) { Text("Ban") }
                }
            )
        }

        if (deleteDialogOpen) {
            AlertDialog(
                onDismissRequest = { deleteDialogOpen = false },
                title = { Text("Confirm Campsite Deletion") },
                text = { Text("Are you sure you want to delete this campsite? This cannot be undone.") },
                dismissButton = {
                    TextButton(onClick = { deleteDialogOpen = false }) { Text("Go Back") }
                },
                confirmButton = {
                    TextButton(onClick = {
                        deleteDialogOpen = false
                        controller.deleteCampsite()
                    }) { Text("Delete") }
                }
            )
        }
    }
}
