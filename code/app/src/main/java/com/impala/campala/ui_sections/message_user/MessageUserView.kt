package com.impala.campala.ui_sections.message_user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.impala.campala.User
import com.impala.campala.UserViewMode
import com.impala.campala.ViewObserver
import com.impala.campala.model.MessageModel

/**
 * The user interface for the messaging feature between users.
 *
 * @param controller The associated controller, allowing for the UI to get information from the model.
 */
class MessageUserView(val controller: MessageUserController) : ViewObserver<MessageUserModel> {
    var currentRole = User.getCurrentRole()
    private var messages = mutableStateListOf<MessageModel>()
    private var threadInfo by mutableStateOf(mapOf("entrantId" to "", "ownerId" to "", "reservationId" to ""))

    /**
     * Updates the UI's information from the Model.
     *
     * @param model The model which stores information for the UI.
     */
    override fun update(model: MessageUserModel): Unit {
        this.currentRole = User.getCurrentRole()
        this.messages.clear()
        this.messages.addAll(model.messages())
        this.threadInfo = model.threadInfo()
    }

    /**
     * Formats LocalDateTime's string output into something prettier.
     *
     * @param string The unformatted LocalDateTime.toString() output.
     */
    fun dateFormatter(string: String): String {

        var year = ""
        for (i in 0..3) {year += string[i]}

        var month = ""
        for (i in 5..6) {month += string[i]}

        var day = ""
        for (i in 8..9) {day += string[i]}

        var time = ""
        for (i in 11..15) {time += string[i]}

        var tod = "AM"
        var hourInInt = time[0].toChar().digitToInt()*10 + time[1].toChar().digitToInt()

        if ( hourInInt > 12) {
            tod = "PM"
            hourInInt = hourInInt % 12
            if (hourInInt == 0)
                hourInInt += 12
            time = String(charArrayOf((hourInInt / 10).digitToChar(),(hourInInt % 10).digitToChar(),time[2],time[3],time[4]))
        }

        return "  " + time + " " + tod + ", " + day + "/" + month + "/" + year + "  "
    }

    /**
     * Renders the messaging UI.
     *
     * @param navController Dictates navigation movement and history; current page, past page etc.
     * @param reservationId References the current reservation that communication is happening for.
     */
    @Composable
    fun render(navController: NavController, reservationId: String) { //TODO: Make it nice!

        var message by remember {
            mutableStateOf("")
        }

        // Redo this on each launch
        LaunchedEffect(reservationId) {
            controller.loadThread(reservationId)
        }

        Scaffold(

            topBar =  {
                Surface(shape = RoundedCornerShape(
                    0.dp)) {
                    Box(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
                        Button(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier.testTag("backButton").align(Alignment.CenterStart) // for testing
                        ) {
                            Text("Back")
                        }

                        // Conversation title. Varies based on current role, defaults to a 3rd party "User [x] and owner [y]", equivalent to what admin would see.
                        Text(
                            text = if (User.getCurrentRole() == UserViewMode.ENTRANT) "Owner"
                            else if (User.getCurrentRole() == UserViewMode.CAMPSITE_OWNER) "Camper"
                            else "Conversation",
                            fontSize = 24.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        Button(
                            onClick = {
                                controller.refresh()
                            },
                            modifier = Modifier.testTag("refreshButton").align(Alignment.CenterEnd)
                        ) {
                            Text(text = "↻", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

            },

            bottomBar = {// text field and button to send message

                Surface(shape = RoundedCornerShape(
                    0.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .navigationBarsPadding()
                            .padding(4.dp),
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
                            modifier = Modifier.testTag("messageInput").width(320.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (message.isNotBlank()) {
                                    controller.send(
                                        message,
                                        owner = User.getCurrentRole() == UserViewMode.CAMPSITE_OWNER
                                    )
                                    message = ""
                                }
                            },
                            modifier = Modifier.testTag("sendMessageButton") // for testing
                        ) {
                            Text(text = "⬆", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, textAlign = TextAlign.Center)
                        }
                    }
                }


            }

        ) { innerPadding ->
            LazyColumn(modifier = Modifier, contentPadding = innerPadding) {
                items(messages) { msg ->
                    Spacer(modifier = Modifier.height(12.dp))

                    var rightAlign = ((msg.fromOwner && currentRole == UserViewMode.CAMPSITE_OWNER) || (!msg.fromOwner && currentRole == UserViewMode.ENTRANT))

                    Column(modifier = Modifier
                        .fillMaxWidth()
                    ){
                        Surface(
                            shape = if (rightAlign) RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 12.dp)
                            else
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomEnd = 12.dp,
                                    bottomStart = 0.dp)
                            ,
                            color = if (rightAlign) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.wrapContentSize().widthIn(max = 260.dp).padding(horizontal = 12.dp).then( if (rightAlign) Modifier.align(Alignment.End) else Modifier.align(Alignment.Start))
                        ) {
                            Column(
                                modifier = Modifier,
                            ) {
                                Text(
                                    text = msg.body,
                                    fontSize = 24.sp,
                                    // This next line decides if the message is left or right alligned, based on if it's an owner message, and what the current role is. It's basically "If you're not the one who sent it, it's on the left."
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                )
                                Text(
                                    text = dateFormatter(msg.localDate().toString()),
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}