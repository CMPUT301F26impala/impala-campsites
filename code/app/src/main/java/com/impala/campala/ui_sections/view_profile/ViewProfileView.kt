package com.impala.campala.ui_sections.view_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.impala.campala.ViewObserver

/**
 * The view for the 'Profile' tab on the entrant screen
 * It displays the info of the profile, and has buttons to edit or delete or do any other necessary actions.
 *
 * @param controller the ViewProfileController that this view uses
 */
class ViewProfileView(val controller: ViewProfileController) : ViewObserver<ViewProfileModel> {
    private var name by mutableStateOf("")
    private var email by mutableStateOf("")
    private var phoneNumber by mutableStateOf("")
    private var showingDeleteConfirmation by mutableStateOf(false)

    override fun update(model: ViewProfileModel) {
        this.name = model.profile?.name ?: ""
        this.email = model.profile?.email ?: ""
        this.phoneNumber = model.profile?.phoneNumber ?: ""
        this.showingDeleteConfirmation = model.showingDeleteConfirmation
    }

    /**
     * The main function that renders the screen
     *
     * @param navController The navigation controller for Android
     */
    @Composable
    fun Render(navController: NavController) {
        LaunchedEffect(Unit) {
            controller.onFirstRender()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                DrawProfilePicture()
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = name.ifBlank { "No name set" },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                DrawContactRow(Icons.Default.Email, email.ifBlank { "No email set" })
                Spacer(modifier = Modifier.height(4.dp))
                DrawContactRow(Icons.Default.Phone, phoneNumber.ifBlank { "No phone number set" })

                Spacer(modifier = Modifier.height(40.dp))
                DrawActionButton(Icons.AutoMirrored.Filled.List, "View App Policies", MaterialTheme.colorScheme.primary, onClick = { controller.onViewAppPoliciesPress(navController) })
                Spacer(modifier = Modifier.height(8.dp))
                DrawActionButton(Icons.Default.Delete, "Delete Profile", MaterialTheme.colorScheme.tertiary, onClick = { controller.onDeleteProfilePress() })

                // Extra room for the edit FAB
                Spacer(modifier = Modifier.height(96.dp))
            }

            ExtendedFloatingActionButton(
                onClick = { controller.onEditProfilePress(navController) },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text("Edit Profile") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp)
            )
        }

        if (showingDeleteConfirmation) {
            DrawDeleteConfirmation()
        }
    }

    @Composable
    private fun DrawDeleteConfirmation() {
        AlertDialog(
            onDismissRequest = { controller.onDeleteCancel() },
            title = { Text("Confirm Deletion") },
            text = {
                Text("Are you sure you want to delete your profile? You will lose any in-progress reservations and will not be able to see your previous data.")
            },
            dismissButton = {
                TextButton(onClick = { controller.onDeleteCancel() }) {
                    Text("Go Back")
                }
            },
            confirmButton = {
                TextButton(onClick = { controller.onDeleteConfirm() }) {
                    Text("Confirm")
                }
            }
        )
    }

    @Composable
    private fun DrawProfilePicture() {
        // TODO: Profile pictures, this gradient is just a placeholder until those exist
        Box(
            modifier = Modifier
                .size(232.dp)
                .clip(RoundedCornerShape(percent = 28))
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFF9E9E9E))))
        )
    }

    @Composable
    private fun DrawContactRow(icon: ImageVector, text: String) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    @Composable
    private fun DrawActionButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
        TextButton(onClick = onClick, colors = ButtonDefaults.textButtonColors(contentColor = color)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label)
        }
    }
}
