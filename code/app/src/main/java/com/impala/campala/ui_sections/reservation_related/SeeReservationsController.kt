package com.impala.campala.ui_sections.reservation_related;

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.google.firebase.Timestamp
import com.impala.campala.ViewObserver
import com.impala.campala.model.ReservationModel
import com.impala.campala.model.ReservationRepository
import java.time.format.DateTimeFormatter
import kotlin.collections.List

public class SeeReservationsController(val view: SeeReservationsView) {
    var resDB = ReservationRepository()
    var reservations: List<ReservationModel> = emptyList()
    var oldReservations: List<ReservationModel> = emptyList()
    var newReservations: List<ReservationModel> = emptyList()

    public suspend fun initialize(uid: String) {
        val currentDate: com.google.firebase.Timestamp = Timestamp.now()
        reservations = resDB.getReservationsByUser(uid).sortedBy { it.startDate }
        oldReservations = reservations.filter {it.endDate < currentDate}
        newReservations = reservations.filter {it.endDate >= currentDate}
    }
}
