package com.impala.campala.ui_sections.create_reservation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.impala.campala.model.ReservationModel
import com.impala.campala.model.ReservationRepository
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class CreateReservationView(
    val controller: CreateReservationController,
    val reservationRepo: ReservationRepository,
    val scope: CoroutineScope
) {
    private var agreedTerms by mutableStateOf(false)
    private var localCampsiteModel by mutableStateOf(controller.model.campsite)
    private var startDate by mutableStateOf(YearMonth.now().atDay(1))
    private var endDate by mutableStateOf(YearMonth.now().atEndOfMonth().plusDays(1))
    private var reserved = mutableStateListOf<ReservationModel>()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Composable
    fun Render(
    ) {
        LaunchedEffect(Unit) {
            for (reservation in reservationRepo.getReservationsByDateRange(
                Timestamp(
                    startDate.atStartOfDay(
                        ZoneId.systemDefault()
                    ).toInstant()
                ), Timestamp(
                    endDate.atStartOfDay(
                        ZoneId.systemDefault()
                    ).toInstant()
                )
            )) {
                reserved.add(reservation)
            }
        }
        Scaffold() {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it)
                    .padding(16.dp)
            ) {
                Text(
                    "Reserve ${localCampsiteModel.name}",
                    style = MaterialTheme.typography.headlineLarge
                )
                if (!agreedTerms) {
                    // it looks tiny for now but thats not my fault
                    Text(
                        "Campsite rules and policies",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    // this could be a markdown renderer at some point if anyone feels fancy
                    Text(localCampsiteModel.rules, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.align(
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = agreedTerms,
                            onCheckedChange = { agreedTerms = true },
                        )
                        Text("I accept", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    DrawCalendar(startDate, endDate)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Composable
    private fun DrawCalendar(startDate: LocalDate, endDate: LocalDate) {
        Grid(
            config = {
                repeat(7) {
                    column(1 / 7f)
                }
                gap(8.dp)
            }
        ) {
            Text("Mon")
            Text("Tue")
            Text("Wed")
            Text("Thu")
            Text("Fri")
            Text("Sat")
            Text("Sun")
            // theres probably a better way to insert blanks
            for (i in 1..(startDate.dayOfWeek.value - 1)) {
                Text("")
            }

            for (date in startDate.datesUntil(endDate)) {
                var backgroundColor = MaterialTheme.colorScheme.primary

                for (reservation in reserved) {
                    if (Timestamp(
                            date.atStartOfDay(
                                ZoneId.systemDefault()
                            ).toInstant()
                        ) in reservation.startDate..reservation.endDate
                    ) {
                        backgroundColor = MaterialTheme.colorScheme.tertiary
                        break
                    }
                }
                Text(
                    "${date.dayOfMonth}",
                    modifier = Modifier
                        .background(
                            color = backgroundColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}