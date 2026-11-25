package com.impala.campala.ui_sections.message_user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.impala.campala.User
import com.impala.campala.UserViewMode
import com.impala.campala.ViewObserver
import com.impala.campala.model.MessageModel
import com.impala.campala.ui_sections.view_lotteries.ViewLotteriesModel

class MessageUserView(val controller: MessageUserController) : ViewObserver<MessageUserModel> {
    var currentRole = User.getCurrentRole()

    override fun update(model: MessageUserModel): Unit {
        this.currentRole = User.getCurrentRole()
    }

    @Composable
    fun render(navController: NavController) { //TODO: Make it nice!

        var message by remember {
            mutableStateOf("")
        }

        var messages by remember {
            mutableStateOf(mutableListOf<MessageModel>())
        }

        // Redo this on each launch
        LaunchedEffect(Unit) {
            controller.refresh()
            messages = controller.messages()
        }

        Column(modifier = Modifier) {

            Row(modifier = Modifier) {
                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.testTag("backButton") // for testing
                ) {
                    Text("Back")
                }

                // Conversation title. Varies based on current role, defaults to a 3rd party "User [x] and owner [y]", equivalent to what admin would see.
                Text(
                    text = if (User.getCurrentRole() == UserViewMode.ENTRANT) controller.threadInfo()["ownerId"]!!
                    else if (User.getCurrentRole() == UserViewMode.CAMPSITE_OWNER) controller.threadInfo()["entrantId"]!!
                    else "User " + controller.threadInfo()["entrantId"]!! + " and owner " + controller.threadInfo()["ownerId"]!!,
                    fontSize = 30.sp
                )

                Button(
                    onClick = {
                        controller.refresh()
                        messages = controller.messages()
                    },
                    modifier = Modifier.testTag("refreshButton") // for testing
                ) {
                    Text("↻")
                }
            }

            LazyColumn {
                items(messages) { msg ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = msg.body,
                        fontSize = 24.sp,
                        // This next line decides if the message is left or right alligned, based on if it's an owner message, and what the current role is. It's basically "If you're not the one who sent it, it's on the left."
                        modifier = if ((msg.fromOwner && currentRole == UserViewMode.CAMPSITE_OWNER) || (!msg.fromOwner && currentRole == UserViewMode.ENTRANT)) Modifier.align(Alignment.End) else Modifier.align(Alignment.Start)
                    )

                    Text(
                        text = msg.localDate().toString(), //TODO: Wow factor, make the time look prettier
                        fontSize = 12.sp
                    )
                }
            }

            // text field and button to send message
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = message,
                    onValueChange = {
                        message = it // update location to new value
                    },
                    label = {
                        Text("Send message...")
                    },
                    modifier = Modifier.testTag("messageInput") // for testing
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            controller.send(message, owner = User.getCurrentRole() == UserViewMode.CAMPSITE_OWNER)
                            message = ""
                        }
                    },
                    modifier = Modifier.testTag("sendMessageButton") // for testing
                ) {
                    Text("⬆")
                }
            }

        }





    }
}