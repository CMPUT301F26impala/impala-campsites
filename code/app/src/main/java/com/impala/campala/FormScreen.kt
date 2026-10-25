package com.impala.campala

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.impala.campala.ui.theme.AppTheme
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.InputChip
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.input.KeyboardType

// same screen component for both create and edit forms
// is isEditing is true then prefill details, if not then its a create form
// FormScreen() for Add
// FormScreen(isEditing = true) for Edit
@Composable
fun FormScreen(isEditing: Boolean = false) {

    // title input box
    var title by remember { mutableStateOf("") }
    // description input box
    var description by remember { mutableStateOf("") }
    // maximum occupancy input box
    var maximumOccupancy by remember { mutableStateOf("") }
    // location dropdown
    var location by remember { mutableStateOf("") }
    // tags
    var tagInput by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {

        // heading
        if (isEditing) {
            Text(
                text = "Edit Campsite",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Text(
                text = "Create Campsite",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title input box
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Campsite Title") },
            placeholder = { Text("Enter the title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Location input box
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Campsite Location") },
            placeholder = { Text("Enter the location") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Campsite Description input box
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Campsite Description") },
            placeholder = { Text("Enter the description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Maximum occupancy input box
        OutlinedTextField(
            value = maximumOccupancy,
            onValueChange = { maximumOccupancy = it },
            label = { Text("Maximum Occupancy") },
            placeholder = { Text("Enter the number here") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // tags input box
        OutlinedTextField(
            value = tagInput,
            onValueChange = {
                if ('\n' in it) {
                    // make a tag when user presses enter
                    val trimmedTag = it.substringBefore("\n").trim()
                    tags.add(trimmedTag)
                    tagInput = ""
                } else {
                    tagInput = it
                }
            },
            label = { Text("Tags") },
            placeholder = { Text("Enter the tag details here") },
            modifier = Modifier.fillMaxWidth()
        )

        // list of the actual tags
        // Flow row so I don't have to figure out wrapping horizontally
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // loop on every tag inside the tags list
            tags.forEach {tag ->
                InputChip(
                    selected = false,
                    // remove the tag if you click anywhere on the tag itself
                    onClick = { tags.remove(tag) },
                    label = { Text(tag) },
                    leadingIcon = {
                        Text("X")
                    }
                )
            }
        }

    } // personal reference, end of column here

}
// test without building the app
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun FormPreview() {
    AppTheme {
        FormScreen()
    }
}