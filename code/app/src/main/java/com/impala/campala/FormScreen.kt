package com.impala.campala

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

/*
TODO:
 1. maxOccupancy conversion logic before saving,
    current function will crash when given anything not a number
 */

/*
new campsites are added using an onSave function passed
into the formScreen by whoever calls the function.
It accepts a parameter fo the type Campsite declared in CampsiteModel
 */

// same screen component for both create and edit forms
// if campsite was not provided (its null) then create for, or else edit form
@Composable
fun FormScreen(
    navController: NavController,
    campsite: CampsiteModel? = null,
    onSave: (CampsiteModel) -> Unit
) {

    // title input box
    var title by remember {
        mutableStateOf(if (campsite == null) "" else campsite.name)
    }
    // location dropdown
    var location by remember {
        mutableStateOf(if (campsite == null) "" else campsite.location)
    }
    // description input box
    var description by remember {
        mutableStateOf(if (campsite == null) "" else campsite.description)
    }
    // maximum occupancy input box
    var maximumOccupancy by remember {
        // need to string here since input box uses string.
        // can be converted to int later on submission
        mutableStateOf(if (campsite == null) "" else campsite.maxOccupancy.toString())
    }
    // tags
    var tagInput by remember { mutableStateOf("") }
    val tags = remember {
        if (campsite == null) {
            mutableStateListOf<String>()
        } else {
            campsite.tags.toMutableStateList()
        }
    }

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
            if (campsite!=null) {
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
                    onValueChange = { title = it },
                    label = { Text("Campsite Title") },
                    placeholder = { Text("Enter the title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Location input box
            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Campsite Location") },
                    placeholder = { Text("Enter the location") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Campsite Description input box
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Campsite Description") },
                    placeholder = { Text("Enter the description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }


            // Maximum occupancy input box
            item {
                OutlinedTextField(
                    value = maximumOccupancy,
                    onValueChange = { maximumOccupancy = it },
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
                            onClick = { tags.remove(tag) },
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
                onClick = {
                    // only works when updating
                    if (campsite != null) {
                        val newCampsite = campsite.copy(
                            name = title,
                            location = location,
                            description = description,
                            maxOccupancy = if (maximumOccupancy == "") 0 else maximumOccupancy.toInt(),
                            tags = tags.toMutableList()
                        )
                        // update new campsite to the actual model
                        // might change later based on MVC
                        onSave(newCampsite)
                    } else {
                        // code for creating a new campsite
                        val newCampsite = CampsiteModel(
                            name = title,
                            location = location,
                            description = description,
                            maxOccupancy = if (maximumOccupancy == "") 0 else maximumOccupancy.toInt(),
                            tags = tags.toMutableList()
                        )
                        // update new campsite to the actual model
                        // might change later based on MVC
                        onSave(newCampsite)
                    }

                }
                // write code for actually saving this new campsite to
            ) {
                if (campsite == null) {
                    Text("Create")
                } else {
                    Text("Save")
                }
            }
        } // personal reference row ends here
    } // bracket for Column of overall screen
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