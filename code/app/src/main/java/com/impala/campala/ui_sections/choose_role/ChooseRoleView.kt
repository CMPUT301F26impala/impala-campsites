package com.impala.campala.ui_sections.choose_role

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.impala.campala.R
import com.impala.campala.User
import com.impala.campala.UserViewMode
import com.impala.campala.ViewObserver

/**
 * The Choose Role Screen
 * This is also the 'welcome' screen, in that it gets shown on first app open, and also shown anytime a user wants to change view modes
 *
 * @param controller the ChooseRoleController instance that is connected to the model
 */
class ChooseRoleView(val controller: ChooseRoleController) : ViewObserver<ChooseRoleModel> {
    private var isSaving by mutableStateOf(false)
    private var chosenRole by mutableStateOf<UserViewMode?>(null)

    override fun update(model: ChooseRoleModel) {
        this.isSaving = model.isSaving
        this.chosenRole = model.chosenRole
    }

    /**
     * The main render function to output the screen
     *
     * @param user the User picking a role
     * @param onRoleChosen called once the role is saved, with the role the user picked
     * @return Unit, but it renders Composable UI as a side effect
     */
    @Composable
    fun Render(user: User, onRoleChosen: (UserViewMode) -> Unit) {
        LaunchedEffect(chosenRole) {
            // This is needed to be able to tell CampalaApp.kt (the root navigation stuff) "Hey, I've picked a role now, close this page and open the proper nav"
            chosenRole?.let { // This is basically (if (chosenRole != null) { onRoleChosen(chosenRole) }
                onRoleChosen(it)
                controller.onRoleChosenHandled() // This clears the chosenRole variable so that next time you come back to this page you can pick a new one
            }
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Icon(
                painterResource(R.drawable.campala_logo),
                contentDescription = "Campala",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(0.8f).height(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Let’s Get You Set Up", style = MaterialTheme.typography.headlineSmall)

            // weight pushes the buttons down to the bottom of the screen
            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Button(onClick = { controller.useAsEntrant(user) }, enabled = !isSaving) {
                    Text("Use as a Camper")
                }
                Button(onClick = { controller.useAsCampsiteOwner(user) }, enabled = !isSaving) {
                    Text("Use as a Campsite Owner")
                }
            }
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
