package com.impala.campala.ui_sections.lottery_details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.impala.campala.ViewObserver
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.ui_sections.components.CardDetail
import com.impala.campala.ui_sections.components.InfoCard
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * The Lottery Details screen a campsite owner sees when they tap one of their lotteries
 *
 * Shows the lottery's dates and spots, its campsites' description and tags, and its waitlist underneath
 * People on the waitlist can be checked, and the Create Notification button sends a notification to everyone checked
 *
 * @param controller the LotteryDetailsController instance that is connected to the model
 */
class LotteryDetailsView(val controller: LotteryDetailsController) : ViewObserver<LotteryDetailsModel> {
    private var isLoaded by mutableStateOf(false)
    private var name by mutableStateOf("")
    private var location by mutableStateOf("")
    private var description by mutableStateOf("")
    private var tags by mutableStateOf<List<String>>(emptyList())
    private var status by mutableStateOf(LotteryStatus.UPCOMING)
    private var spotsRemaining by mutableIntStateOf(0)
    private var waitlistCount by mutableIntStateOf(0)
    private var waitlistCapacity by mutableIntStateOf(0)
    private var lotteryStart by mutableStateOf<Timestamp?>(null)
    private var lotteryEnd by mutableStateOf<Timestamp?>(null)
    private var bookingStart by mutableStateOf<Timestamp?>(null)
    private var bookingEnd by mutableStateOf<Timestamp?>(null)
    private var waitlist by mutableStateOf<List<EntrantProfileModel>>(emptyList())
    private var selected by mutableStateOf<Set<String>>(emptySet())
    private var showNotifModal by mutableStateOf(false)
    private var notifTitle by mutableStateOf("")
    private var notifBody by mutableStateOf("")

    // No KDoc because that's described in the ViewObserver contract
    override fun update(model: LotteryDetailsModel) {
        val lottery = model.lottery
        isLoaded = lottery != null
        name = model.name
        location = model.location
        description = model.description
        tags = model.tags
        status = model.status
        spotsRemaining = model.spotsRemaining
        waitlistCount = lottery?.waitlistIds?.size ?: 0
        waitlistCapacity = lottery?.waitlistCapacity ?: 0
        lotteryStart = lottery?.lotteryStartTime
        lotteryEnd = lottery?.lotteryEndTime
        bookingStart = lottery?.registrationStartTime
        bookingEnd = lottery?.registrationEndTime
        waitlist = model.waitlist
        // Copied so Compose sees a new Set, the model keeps changing the same one so it would look unchanged
        selected = model.selected.toSet()
        showNotifModal = model.showNotifModal
        notifTitle = model.notifTitle
        notifBody = model.notifBody
    }

    /**
     * The main render function to output the screen
     *
     * Also asks the controller to load the lottery whenever the screen is shown with a new id
     *
     * @param navController the root NavController, used to open the edit lottery screen
     * @param lotteryId the ID of the lottery to show
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    fun Render(navController: NavController, lotteryId: String) {
        LaunchedEffect(lotteryId) {
            controller.onOpen(lotteryId)
        }
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { controller.onEditPress(navController, lotteryId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit lottery", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                if (isLoaded) {
                    RenderInfo()
                    RenderTextSection("Description", description.ifEmpty { "No description" })
                    RenderTextSection("Tags", tags.joinToString().ifEmpty { "No tags" })
                    RenderSectionTitle("Waitlist ($waitlistCount/$waitlistCapacity)")
                }
                RenderWaitlist()
                // Room for the Create Notification button so it doesn't cover the last person
                Spacer(modifier = Modifier.height(96.dp))
            }
            RenderNotifyButton()
        }
    }

    @Composable
    private fun RenderInfo() {
        Spacer(modifier = Modifier.height(8.dp))
        IconLabel(Icons.Default.LocationOn, location)
        when (status) {
            LotteryStatus.UPCOMING -> IconLabel(Icons.Outlined.Info, "Upcoming Lottery")
            LotteryStatus.ACTIVE -> IconLabel(Icons.Default.Check, "Active Lottery")
            LotteryStatus.DRAWN -> IconLabel(Icons.Default.Check, "Lottery Drawn")
        }
        Spacer(modifier = Modifier.height(8.dp))
        IconLabel(Icons.Outlined.Person, "$spotsRemaining Spots Remaining")
        Spacer(modifier = Modifier.height(8.dp))
        lotteryEnd?.let { IconLabel(Icons.Default.DateRange, "Draws on ${formatDay(it)}") }
        InfoText("Registration Period: ${formatRange(lotteryStart, lotteryEnd)}")
        InfoText("Booking Period: ${formatRange(bookingStart, bookingEnd)}")
    }

    @Composable
    private fun RenderWaitlist() {
        Column(
            modifier = Modifier.padding(0.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            waitlist.forEach { camper ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = camper.userId in selected,
                        onCheckedChange = { controller.onCheckedChange(camper) }
                    )
                    InfoCard(
                        null, camper.name.ifEmpty { "Unknown" }, listOf(
                        { CardDetail(Icons.Outlined.AccountCircle, camper.userId) }
                    ))
                }
            }
        }
    }

    @Composable
    private fun RenderNotifyButton() {
        val canNotify = selected.isNotEmpty()
        // FABs don't have an enabled flag, so the disabled look uses the same colours a disabled Button does
        // The container is blended onto the background so the list doesn't show through it
        val disabledContainer = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            .compositeOver(MaterialTheme.colorScheme.background)
        val disabledContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ExtendedFloatingActionButton(
            onClick = { if (canNotify) controller.notifyBtn() },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            text = { Text("Create Notification") },
            containerColor = if (canNotify) FloatingActionButtonDefaults.containerColor else disabledContainer,
            contentColor = if (canNotify) contentColorFor(FloatingActionButtonDefaults.containerColor) else disabledContent,
            elevation = if (canNotify) FloatingActionButtonDefaults.elevation() else FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            // This sits on top of the scrolling Column, the Box in Render is what lets the two overlap
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
                // Lets TalkBack (and UI tests) know the button can't be used yet
                .semantics { if (!canNotify) disabled() }
        )
        if (showNotifModal)
            AlertDialog(
                onDismissRequest = { controller.modalDismiss() },
                title = { Text("Create notification") },
                text = {
                    Column() {
                        OutlinedTextField(
                            label = { Text("Title") },
                            value = notifTitle,
                            onValueChange = { controller.onNotifTitleInput(it) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            label = { Text("Content") },
                            value = notifBody,
                            onValueChange = { controller.onNotifBodyInput(it) },
                            minLines = 5
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { controller.sendNotif() },
                        enabled = selected.size > 0
                    ) { Text("Send notification") }
                },
                dismissButton = {
                    TextButton(onClick = { controller.modalDismiss() }) { Text("Cancel") }
                }
            )
    }

    @Composable
    private fun RenderSectionTitle(title: String) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
    }

    @Composable
    private fun RenderTextSection(title: String, content: String) {
        RenderSectionTitle(title)
        Text(content, style = MaterialTheme.typography.bodyMedium)
    }

    @Composable
    private fun IconLabel(icon: ImageVector, text: String) {
        Row(
            modifier = Modifier.padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }

    @Composable
    private fun InfoText(text: String) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }

    private fun formatDay(time: Timestamp): String {
        return DateTimeFormatter.ofPattern("MMMM d, yyyy")
            .withZone(ZoneOffset.UTC)
            .format(time.toInstant())
    }

    private fun formatRange(start: Timestamp?, end: Timestamp?): String {
        if (start == null || end == null) return ""
        val startDay = start.toInstant().atZone(ZoneOffset.UTC).toLocalDate()
        val endDay = end.toInstant().atZone(ZoneOffset.UTC).toLocalDate()
        val sameYear = startDay.year == endDay.year
        val sameMonth = sameYear && startDay.month == endDay.month
        val startText = startDay.format(DateTimeFormatter.ofPattern(if (sameYear) "MMMM d" else "MMMM d, yyyy"))
        val endText = endDay.format(DateTimeFormatter.ofPattern(if (sameMonth) "d, yyyy" else "MMMM d, yyyy"))
        return if (sameMonth) "$startText-$endText" else "$startText - $endText"
    }
}
