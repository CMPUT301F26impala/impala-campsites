package com.impala.campala.ui_sections.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

// This is a location picker using the maplibre-compose library
// I asked Claude Opus 5.5 if a free maps API for jetpack compose existed that I could use
// It said that the google maps API requried an API key, but maplibre-compose didn't
// So that was the rationale behind choosing this library
// I then followed the docs at https://maplibre.org/maplibre-compose/styling/
// To create this file, the code is my own, I just used AI for the research piece.

private const val MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

// Starts you in Alberta
private const val DEFAULT_LATITUDE = 54.5
private const val DEFAULT_LONGITUDE = -115.0
private const val DEFAULT_ZOOM = 4.5
private val PIN_SIZE = 48.dp

/**
 * A reusable component that allows a user to pick a location on a map and spits out the coordinates
 *
 * @param onLocationChange Called with the coordinates of the pinned location everytime the camera stops moving and at the beginning.
 * @param modifier General android styling tool
 * @param initialLatitude Latitude the map starts on
 * @param initialLongitude Longitude the map starts on
 */
@Composable
fun LocationPicker(
    onLocationChange: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier = Modifier,
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
) {
    val mapState = rememberMapState(
        baseStyle = BaseStyle.Uri(MAP_STYLE_URL),
        initialCameraPosition = CameraPosition(
            center = Position(
                longitude = initialLongitude ?: DEFAULT_LONGITUDE,
                latitude = initialLatitude ?: DEFAULT_LATITUDE
            ),
            zoom = DEFAULT_ZOOM
        )
    )

    // This snapshotFlow allows us to not spam updates while the camera is moving super fast
    // It only fires after the camera stops moving.
    LaunchedEffect(mapState, onLocationChange) {
        snapshotFlow { if (mapState.isCameraMoving) null else mapState.cameraPosition.center }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { center -> onLocationChange(center.latitude, center.longitude) }
    }

    Box(modifier = modifier) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            state = mapState
        )

        // This is the Pin, it's not a part of the map library but is just something I absolutely position on top
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Selected location",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .size(PIN_SIZE)
                .offset(y = -PIN_SIZE * 10 / 24)
        )
    }
}
