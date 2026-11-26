package com.impala.campala.ui_sections.visited_map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.GeoPoint
import com.impala.campala.ViewObserver
import com.impala.campala.ui_sections.components.MapDisplay

/**
 * The screen with a map of every campsite the entrant has been to
 *
 * @param controller the VisitedMapController that this view uses
 */
class VisitedMapView(val controller: VisitedMapController) : ViewObserver<VisitedMapModel> {
    private var points by mutableStateOf<List<GeoPoint>>(emptyList())

    override fun update(model: VisitedMapModel) {
        this.points = model.points
    }

    init {
        update(controller.model)
    }

    /**
     * The main function that renders the screen
     *
     * @param navController the root NavController, used to go back to the profile tab
     */
    @Composable
    fun Render(navController: NavController) {
        LaunchedEffect(Unit) {
            controller.onFirstRender()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            Text(
                text = "Places You've Been",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                MapDisplay(
                    points = points,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = { controller.onBackPress(navController) }) {
                    Text("Back")
                }
            }
        }
    }
}
