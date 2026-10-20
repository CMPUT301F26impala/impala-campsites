package com.impala.campala

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UiExamples() {
    Column() {
        Text("Example Title Text", style= MaterialTheme.typography.headlineLarge)
        Text("Example Label Text", style= MaterialTheme.typography.labelLarge)
        Text("Example Body Text", style= MaterialTheme.typography.bodyMedium)
        Button(
            onClick = {},
        ) {
            Text("Example Inline Button")
        }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text("Example Deep Color Button")
        }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            )
        ) {
            Text("Example Secondary Button")
        }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        ) {
            Text("Example Deep Secondary Button")
        }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("Example Tertiary Button")
        }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Text("Example Deep Tertiary Button")
        }
        // For floating action button to ACTUALLY be floating in the bottom corner
        // You need to put it on the Scaffold() at the root of a page.
        FloatingActionButton(
            onClick = {},
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Row(modifier = Modifier.padding(horizontal=16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete Icon",
                )
                Text("Example Floating Action Button")
            }
        }
    }
}