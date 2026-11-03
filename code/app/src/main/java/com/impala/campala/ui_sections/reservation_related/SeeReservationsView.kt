package com.impala.campala.ui_sections.reservation_related

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.impala.campala.ViewObserver
import com.impala.campala.model.ReservationRepository
import com.impala.campala.ui.theme.tertiaryContainerDark
import com.impala.campala.ui.theme.tertiaryContainerLight
import com.impala.campala.ui.theme.tertiaryDark
import com.impala.campala.ui.theme.tertiaryLight
import java.time.format.DateTimeFormatter

class SeeReservationsView(val controller: SeeReservationsController) {

    private enum class UiTarget { SCREEN_A, SCREEN_B }

    @Composable
    fun mainView() {

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
        LazyColumn(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            items(controller.newReservations) { res ->
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = res.campsiteId, //TODO: Please verify this is the campsite's name and not a code!
                    fontSize = 24.sp
                )

                Button(modifier = Modifier,
                    onClick = {
                        //go to messages
                    }) {
                    Text(
                        text = "View Messages",
                        fontSize = 24.sp
                    )
                }

                Button(modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = tertiaryContainerDark, contentColor = tertiaryDark), //TODO make sure this doesn't look dumb and wrong
                    onClick = {
                        //cancel reservation
                    }) {
                    Text(
                        text = "Cancel Reservation",
                        fontSize = 24.sp,
                        color = tertiaryDark // This may be redundant
                    )
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
                    text = res.campsiteId, //TODO: ditto
                    fontSize = 24.sp
                )

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
}