package com.impala.campala.ui_sections.owner_notifs

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.impala.campala.ViewObserver
import com.impala.campala.model.NotificationModel
import com.impala.campala.ui.theme.AppTheme
import com.impala.campala.ui_sections.components.CardDetail
import com.impala.campala.ui_sections.components.InfoCard

class OwnerNotifsView(val controller: OwnerNotifsController) : ViewObserver<OwnerNotifsModel> {
    var notifs by mutableStateOf<List<NotificationModel>>(emptyList())
    var notifRecStrs by mutableStateOf<List<String>>(emptyList())
    override fun update(model: OwnerNotifsModel) {
        notifs = model.notifs
        notifRecStrs = model.notifRecStrs
    }

    @Composable
    fun Render() {
        LaunchedEffect(Unit) {
            controller.onLoad()
        }
        Log.i("ownernotifs",notifs.toString())
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(notifs) { i, notif ->
                Column() {
                    val names = notifRecStrs[i]
                    // i dont know what the image would actually be
                    // the storyboard does show campsite/lottery info attached to the notifs
                    // thats not how we're modeling notifs though, so i wont be doing that for now
                    InfoCard(
                        null, notif.title, listOf(
                            {
                                if (notif.sent) CardDetail(Icons.Default.Done, "Sent")
                                else CardDetail(Icons.Default.Info, "Processing")
                            },
                            {
                                val maxLen = 30
                                CardDetail(
                                    Icons.Default.AccountBox,
                                    if (names.length <= maxLen) names else names.take(maxLen - 3) + "..."
                                )
                            }
                        ))
                    Text(text = notif.body, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun OwnerNotifsViewPreview() {
    val model = OwnerNotifsModel()
    val controller = OwnerNotifsController(model)
    val view = OwnerNotifsView(controller)

    view.notifs = listOf(
        NotificationModel(
            "Hello",
            LoremIpsum(20).values.first()
        ),
        NotificationModel(
            "Hello",
            LoremIpsum(20).values.first(),
            sent = true
        ),
    )
    view.notifRecStrs =
        listOf(
            "a, b",
            "a, b",
        )

    AppTheme() {
        Scaffold() {
            view.Render()
        }
    }
}