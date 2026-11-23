package com.impala.campala.ui_sections.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun formatDate(timestamp: Timestamp): String {
    return timestamp.toInstant().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("MMM d"))
}

@Composable
fun DateRange(
    start: Timestamp,
    end: Timestamp
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            Icons.Default.DateRange,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "${formatDate(start)} - ${formatDate(end)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}