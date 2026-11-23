package com.impala.campala.ui_sections.waitlist

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.impala.campala.ViewObserver
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.ui.theme.AppTheme
import com.impala.campala.ui_sections.components.CardDetail
import com.impala.campala.ui_sections.components.InfoCard

class WaitlistView(val controller: WaitlistController) : ViewObserver<WaitlistModel> {
    var waitlist by mutableStateOf<List<EntrantProfileModel>>(emptyList())
    var selected by mutableStateOf<Set<String>>(emptySet())
    var showNotifModal by mutableStateOf(false)
    var notifTitle by mutableStateOf("")
    var notifBody by mutableStateOf("")

    override fun update(model: WaitlistModel) {
        waitlist = model.waitlist
        selected = model.selected
        showNotifModal = model.showNotifModal
        notifTitle = model.notifTitle
        notifBody = model.notifBody
    }

    @Composable
    fun Render(navController: NavController, lotteryId: String) {
        LaunchedEffect(lotteryId) {
            controller.onOpen(lotteryId)
        }
        var selectedTab by remember { mutableIntStateOf(1) }
        Column() {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                    },
                    text = { Text("Entrants") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                    },
                    text = { Text("Waitlist") }
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(0.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // TODO: list of entrants when entrant tab selected
                //  (also i dont see a figma frame for that one)
                items(waitlist) { camper ->
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
        ExtendedFloatingActionButton(
            onClick = { controller.notifyBtn() },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            text = { Text("Create notification") },
            // This sits on top of the list, the tab Box in the nav is what lets the two overlap
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
        )
        // i was gonna add a back button here but i dont know how to make multiple floating buttons
        // and i cant be bothered, so youre gonna have to use the device's back button
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
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun WaitlistPreview() {
    val model = WaitlistModel()
    val controller = WaitlistController(model)
    val view = WaitlistView(controller)

    view.waitlist = listOf(
        EntrantProfileModel(

        ),
        EntrantProfileModel(

        ),
    )

    AppTheme {
        Scaffold() {
            view.Render(NavController(LocalContext.current), "")
        }
    }
}