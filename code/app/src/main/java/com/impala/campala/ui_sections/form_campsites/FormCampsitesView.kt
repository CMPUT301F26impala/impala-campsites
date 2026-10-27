package com.impala.campala.ui_sections.form_campsites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.InputChip
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.impala.campala.model.CampsiteModel
import com.impala.campala.ViewObserver
import org.jetbrains.annotations.Debug


/*
TODO:
 1. maxOccupancy conversion logic before saving,
    current function will crash when given anything not a number or empty
 */

/*
new campsites are added using an onSave function passed
into the formScreen by whoever calls the function.
It accepts a parameter fo the type Campsite declared in CampsiteModel
 */

// same screen component for both create and edit forms
// if campsite was not provided (its null) then create for, or else edit form

class FormCampsitesView(
    val controller: FormCampsitesController
): ViewObserver<CampsiteFormModel> {

    private var title by mutableStateOf("")
    private var location by mutableStateOf("")
    private var description by mutableStateOf("")
    private var maximumOccupancy by mutableStateOf("")
    private var tags by mutableStateOf<List<String>>(emptyList())
    private var tagInput by mutableStateOf("")
    private var isEditing by mutableStateOf(false)

    override fun update(model: CampsiteFormModel) {
        title = model.title
        location = model.location
        description = model.description
        maximumOccupancy = model.maximumOccupancy
        tags = model.tags.toList()
        isEditing = model.campsite != null
    }

    init {
        update(controller.model)
    }

    @Composable
    fun Render(navController: NavController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // heading
                if (isEditing == true) {
                    item {
                        Text(
                            text = "Edit Campsite",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "Create Campsite",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Title input box
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { controller.onTitleChange(it) },
                        label = { Text("Campsite Title") },
                        placeholder = { Text("Enter the title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Location input box
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { controller.onLocationChange(it) },
                        label = { Text("Campsite Location") },
                        placeholder = { Text("Enter the location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Campsite Description input box
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { controller.onDescriptionChange(it) },
                        label = { Text("Campsite Description") },
                        placeholder = { Text("Enter the description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }


                // Maximum occupancy input box
                item {
                    OutlinedTextField(
                        value = maximumOccupancy,
                        onValueChange = { controller.onMaximumOccupancyChange(it) },
                        label = { Text("Maximum Occupancy") },
                        placeholder = { Text("Enter the number here") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // tags input box
                item {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = {
                            if ('\n' in it) {
                                // make a tag when user presses enter
                                val trimmedTag = it.substringBefore("\n").trim()
                                controller.onAddTag(trimmedTag)
                                tagInput = ""
                            } else {
                                tagInput = it
                            }
                        },
                        label = { Text("Tags") },
                        placeholder = { Text("Enter the tag details here") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // list of the actual tags
                // Flow row so I don't have to figure out wrapping horizontally
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // loop on every tag inside the tags list
                        tags.forEach { tag ->
                            InputChip(
                                selected = false,
                                // remove the tag if you click anywhere on the tag itself
                                onClick = { controller.onRemoveTag(tag) },
                                label = { Text(tag) },
                                leadingIcon = {
                                    Text("X")
                                }
                            )
                        }
                    }
                }

                // image white placeholder, will be changed later
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                    )
                }

                // button to pick an image from gallery
                item {
                    Text(
                        text = "Upload Alternative Image",
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } // personal reference, end of lazy column here

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // cancel button
                Button(
                    // go back to previous page
                    onClick = { navController.popBackStack() }
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(24.dp))

                Button(
                    onClick = {},
                    enabled = false
                ) {
                    if (isEditing) {
                        Text("Save")
                    } else {
                        Text("Create")
                    }
                }
            } // personal reference row ends here
        } // bracket for Column of overall screen
    }
}

/*
for testing as a preview without building the app

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun FormPreview() {
    AppTheme {
        FormScreen(navController = rememberNavController())
    }
}
 */