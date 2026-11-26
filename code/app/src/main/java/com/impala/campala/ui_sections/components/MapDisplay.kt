package com.impala.campala.ui_sections.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.GeoPoint
import com.impala.campala.ui.theme.onPrimaryContainerLight
import org.maplibre.compose.camera.CameraFit
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.DpPadding
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.Position

private const val MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

private const val DEFAULT_LATITUDE = 54.5
private const val DEFAULT_LONGITUDE = -115.0
private const val DEFAULT_ZOOM = 4.5
// This is MAX zoom, so it'll zoom out to fit more points, but this is as close as it gets.
private const val MAX_FIT_ZOOM = 12.0
private val FIT_PADDING = 48.dp

private val PIN_COLOR = onPrimaryContainerLight
private val PIN_SIZE = 36.dp

/**
 * A map that shows a list of points as dots, and zooms so they're all visible
 *
 * @param points The points to show, the map shows Alberta if this is empty
 * @param modifier Android styling
 */
@Composable
fun MapDisplay(
    points: List<GeoPoint>,
    modifier: Modifier = Modifier,
) {
    val positions = points.map { Position(longitude = it.longitude, latitude = it.latitude) }

    val currentPositions by rememberUpdatedState(positions)

    val mapState = rememberMapState(
        baseStyle = BaseStyle.Uri(MAP_STYLE_URL),
        initialCameraPosition = CameraPosition(
            center = Position(longitude = DEFAULT_LONGITUDE, latitude = DEFAULT_LATITUDE),
            zoom = DEFAULT_ZOOM
        )
    ) {
        // This is basically the layer where things can be rendered OVER the map
        val source = rememberGeoJsonSource(GeoJsonData.Features(MultiPoint(currentPositions)))
        // This helps basically convert the pin icon into something that can be drawn here
        val pinPainter = rememberVectorPainter(Icons.Filled.LocationOn)
        SymbolLayer(
            id = "map-display-points",
            source = source,
            iconImage = image(
                pinPainter,
                size = DpSize(PIN_SIZE, PIN_SIZE),
                colorFilter = ColorFilter.tint(PIN_COLOR)
            ),
            // Bottom puts the pin's tip on the point instead of the middle of the icon
            iconAnchor = const(SymbolAnchor.Bottom),
            // Without this, pins that overlap each other get hidden
            iconAllowOverlap = const(true)
        )
    }

    // Moves the camera so every point fits, re-runs whenever the list of points changes
    LaunchedEffect(mapState, positions) {
        if (positions.isEmpty()) {
            return@LaunchedEffect
        }
        // this works out what position/zoom (camera) will fit all of the points we have
        val fitted = mapState.cameraForCoordinates(
            coordinates = positions,
            fit = CameraFit(fitPadding = DpPadding(FIT_PADDING, FIT_PADDING, FIT_PADDING, FIT_PADDING))
        )
        mapState.setCameraPosition(fitted.copy(zoom = minOf(fitted.zoom, MAX_FIT_ZOOM)))
    }

    MaplibreMap(
        modifier = modifier,
        state = mapState
    )
}
