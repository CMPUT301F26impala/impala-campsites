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

// same screen component for both create and edit forms
// is isEditing is true then prefill details, if not then its a create form
// FormScreen() for Add
// FormScreen(isEditing = true) for Edit
@Composable
fun FormScreen(isEditing: Boolean = false) {

    // title input box
    var title by remember { mutableStateOf("") }

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
        FormScreen(isEditing = true)
    }
}