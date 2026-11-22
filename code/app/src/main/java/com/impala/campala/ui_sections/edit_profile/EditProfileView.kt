package com.impala.campala.ui_sections.edit_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.impala.campala.ViewObserver

/**
 * The Edit Profile, or Profile Form screen.
 *
 * @param controller the EditProfileController used to send user actions to the Model
 */
class EditProfileView(val controller: EditProfileController) : ViewObserver<EditProfileModel> {
    private var name by mutableStateOf("")
    private var email by mutableStateOf("")
    private var phoneNumber by mutableStateOf("")

    /**
     * Updates the view's composable values with the model's truth
     *
     * @param model the EditProfileModel containing the updated information
     */
    override fun update(model: EditProfileModel) {
        name = model.name
        email = model.email
        phoneNumber = model.phoneNumber
    }

    /**
     * The main render function to output the screen
     *
     * @param navController the root NavController, used to go back once the profile is saved
     */
    @Composable
    fun Render(navController: NavController) {
        LaunchedEffect(Unit) {
            controller.onOpen()
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { controller.onNameChange(it) },
                        label = { Text("Name") },
                        placeholder = { Text("Enter your name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { controller.onEmailChange(it) },
                        label = { Text("Email") },
                        placeholder = { Text("Enter your email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { controller.onPhoneNumberChange(it) },
                        label = { Text("Phone Number") },
                        placeholder = { Text("Enter your phone number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { navController.popBackStack() }) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(24.dp))

                Button(onClick = { controller.onSaveClick(navController) }) {
                    Text("Save")
                }
            }
        }
    }
}
