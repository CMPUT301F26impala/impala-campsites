package com.impala.campala.ui_sections.view_lotteries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.impala.campala.UserViewMode
import com.impala.campala.ViewObserver
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The View Lotteries screen component that handles both the entrant and campsite owner versions of this screen
 *
 * For an entrant this lists the lotteries they've joined, when they draw, and whether they won (with a Reserve button if so)
 * For a campsite owner this lists the active lotteries they've created, with an Add Lottery button
 *
 * @param controller the ViewLotteriesController instance that is connected to the model
 */
class ViewLotteriesView(val controller: ViewLotteriesController) : ViewObserver<ViewLotteriesModel> {
    private var lotteries by mutableStateOf<List<LotteryListItem>>(emptyList())
    private var viewMode by mutableStateOf(UserViewMode.ENTRANT)

    // No KDoc because that's described in the ViewObserver contract
    override fun update(model: ViewLotteriesModel) {
        this.lotteries = model.visibleLotteries
        this.viewMode = model.viewMode
    }

    /**
     * The main render function to output the screen
     *
     * Also uses LaunchedEffect to make the controller load the lotteries
     *
     * @param navController the root NavController, used to open other screens
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    fun Render(navController: NavController) {
        // Get the lotteries every time the screen is shown, so returning from the add/edit form shows fresh data
        LaunchedEffect(Unit) {
            controller.onFirstRender()
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            // Bottom padding so the last row can scroll out from under the Add Lottery button
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            if (lotteries.isEmpty()) {
                item {
                    Text(
                        "No lotteries yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(lotteries, key = { it.lottery.id }) { item ->
                if (viewMode == UserViewMode.CAMPSITE_OWNER) {
                    OwnerLotteryRenderItem(item, navController)
                } else {
                    EntrantLotteryRenderItem(item, navController)
                }
            }
        }
        if (viewMode == UserViewMode.CAMPSITE_OWNER) {
            ExtendedFloatingActionButton(
                onClick = { controller.onAddLotteryPress(navController) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Lottery") },
                // Copy-pasted from search campsites screen
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.BottomEnd).padding(bottom = 16.dp)
            )
        }
    }

    @Composable
    private fun OwnerLotteryRenderItem(item: LotteryListItem, navController: NavController) {
        Column(modifier = Modifier.fillMaxWidth().clickable { controller.onLotteryPress(item, navController) }) {
            Text(item.campsite?.name ?: "Unknown Campsite", style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconLabel(Icons.Default.LocationOn, item.campsite?.location ?: "", modifier = Modifier.weight(1f))
                IconLabel(Icons.Outlined.Person, "${item.spotsRemaining} Spots Remaining")
            }
            IconLabel(Icons.AutoMirrored.Filled.List, "Waitlist: ${item.waitlistNames.joinToString("; ").ifEmpty { "none" }}")
        }
    }

    @Composable
    private fun EntrantLotteryRenderItem(item: LotteryListItem, navController: NavController) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.campsite?.name ?: "Unknown Campsite", style = MaterialTheme.typography.headlineMedium)
                when (item.entrantStatus) {
                    EntrantLotteryStatus.WON -> IconLabel(Icons.Default.CheckCircle, "You Won!")
                    EntrantLotteryStatus.ON_WAITLIST -> IconLabel(Icons.AutoMirrored.Filled.List, "On waitlist, draws on ${formatDrawTime(item.lottery.lotteryEndTime)}")
                    EntrantLotteryStatus.WAITING_FOR_DRAW, null -> IconLabel(Icons.Default.DateRange, "Draws on ${formatDrawTime(item.lottery.lotteryEndTime)}")
                }
            }
            if (item.entrantStatus == EntrantLotteryStatus.WON) {
                Button(onClick = { controller.onReservePress(item, navController) }) {
                    Text("Reserve")
                }
            }
        }
    }

    @Composable
    private fun IconLabel(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    // Formats like "Nov 1, 2026 at 6:00PM"
    // Made with the help of Claude Opus 5.5, asking it for a way to format dates into human readable strings in kotlin
    private fun formatDrawTime(time: Timestamp): String {
        return DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mma")
            .withZone(ZoneId.systemDefault())
            .format(time.toInstant())
    }
}
