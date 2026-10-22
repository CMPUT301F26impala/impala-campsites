package com.impala.campala.entrant.search_campsites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.impala.campala.ViewObserver
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * The 'Browse Campsites' screen
 *
 * This class has no useful logic; it's just a documentation example.
 *
 * @param controller the SearchCampsitesController instance that is connected to the model
 */
class SearchCampsitesView(val controller: SearchCampsitesController): ViewObserver<SearchCampsitesModel> {
    private var searchString by mutableStateOf("");
    private var minimumOccupantsFilter by mutableStateOf<Int?>(null)
    private var locationFilter by mutableStateOf<String?>(null);
    private var dateFilter by mutableStateOf<LocalDate?>( null);

    override fun update(model: SearchCampsitesModel) {
        this.searchString = model.searchString
        this.minimumOccupantsFilter = model.minimumOccupantsFilter
        this.locationFilter = model.locationFilter
        this.dateFilter = model.dateFilter
    }

    /**
     * The main render function to output the screen
     * @return Unit, but it does render Composable UI as a side effect
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Render() {
        Column() {
            // https://developer.android.com/develop/ui/compose/components/search-bar
            SearchBar(
                modifier = Modifier.fillMaxWidth(),
                expanded = false, // Search bars by default expand when you click them and show expanded content, by forcing expanded = false here we just ignore that and use it like a big text input
                onExpandedChange = {},
                inputField = {
                    SearchBarDefaults.InputField(
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        query=searchString,
                        onQueryChange = { controller.onSearchBarInputChange(it) },
                        placeholder = { Text("Search") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") }
                    )
                }
            ) {}
            Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateFilter(modifier=Modifier.weight(1f))
                LocationFilter(modifier = Modifier.weight(1f))
                CapacityFilter(modifier = Modifier.weight(1f))
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DateFilter(modifier: Modifier = Modifier) {
        val datePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)
        var datePickerModalOpen by remember { mutableStateOf(false) }
        FilterChip(
            modifier=modifier,
            selected = datePickerModalOpen,
            onClick = {
                datePickerModalOpen = true
            },
            label = { Text(text=if (dateFilter == null) "Date" else dateFilter!!.format(DateTimeFormatter.ofPattern("MMM d")), style=MaterialTheme.typography.labelLarge) },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Date Icon") }
        )
        if (datePickerModalOpen) {
            // https://developer.android.com/develop/ui/compose/components/datepickers#modal
            DatePickerDialog(
                onDismissRequest = {
                    datePickerModalOpen = false;
                },
                confirmButton = {
                    TextButton (onClick = {
                        datePickerModalOpen = false;
                        controller.onDateChange(datePickerState.getSelectedDate())
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        datePickerModalOpen = false;
                    }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    @Composable
    private fun LocationFilter(modifier: Modifier = Modifier) {
        var locationDropdownOpen by remember { mutableStateOf(false) }
        Box(
            modifier = modifier,
        ) {
            FilterChip(
                modifier = Modifier.fillMaxWidth(),
                selected = locationDropdownOpen,
                onClick = {
                    locationDropdownOpen = !locationDropdownOpen
                },
                label = { Text(text=if (locationFilter == null) "Location" else locationFilter!!, style=MaterialTheme.typography.labelLarge) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Date Icon") }
            )
            DropdownMenu(expanded=locationDropdownOpen, onDismissRequest = { locationDropdownOpen = false }) {
                DropdownMenuItem(
                    text={ Text("Jasper") },
                    onClick={ locationDropdownOpen = false; controller.onLocationChange("Jasper") }
                )
                DropdownMenuItem(
                    text={ Text("Banff") },
                    onClick={ locationDropdownOpen = false; controller.onLocationChange("Banff") }
                )
                DropdownMenuItem(
                    text={ Text("Lake Louise") },
                    onClick={ locationDropdownOpen = false; controller.onLocationChange("Lake Louise") }
                )
            }
        }
    }
    @Composable
    private fun CapacityFilter(modifier: Modifier = Modifier) {
        var capacityDropdownOpen by remember { mutableStateOf(false) }
        Box(
            modifier = modifier,
        ) {
            FilterChip(
                modifier = Modifier.fillMaxWidth(),
                selected = capacityDropdownOpen,
                onClick = {
                    capacityDropdownOpen = !capacityDropdownOpen
                },
                label = { Text(text=if (minimumOccupantsFilter == null) "Capacity" else minimumOccupantsFilter!!.toString() + " People", style=MaterialTheme.typography.labelLarge) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person Icon") }
            )
            DropdownMenu(expanded=capacityDropdownOpen, onDismissRequest = { capacityDropdownOpen = false }) {
                DropdownMenuItem(
                    text={ Text("Any Capacity") },
                    onClick={ capacityDropdownOpen = false; controller.onCapacityChange(null) }
                )
                DropdownMenuItem(
                    text={ Text("1") },
                    onClick={ capacityDropdownOpen = false; controller.onCapacityChange(1) }
                )
                DropdownMenuItem(
                    text={ Text("2") },
                    onClick={ capacityDropdownOpen = false; controller.onCapacityChange(2) }
                )
                DropdownMenuItem(
                    text={ Text("3") },
                    onClick={ capacityDropdownOpen = false; controller.onCapacityChange(3) }
                )
                DropdownMenuItem(
                    text={ Text("4") },
                    onClick={ capacityDropdownOpen = false; controller.onCapacityChange(4) }
                )
                DropdownMenuItem(
                    text={ Text("5") },
                    onClick={ capacityDropdownOpen = false; controller.onCapacityChange(5) }
                )
                DropdownMenuItem(
                    text={ Text("6") },
                    onClick={ capacityDropdownOpen = false; controller.onCapacityChange(6) }
                )
                DropdownMenuItem(
                    text={ Text("7") },
                    onClick={ capacityDropdownOpen = false; controller.onCapacityChange(7) }
                )
                DropdownMenuItem(
                    text={ Text("8") },
                    onClick={ capacityDropdownOpen = false; controller.onCapacityChange(8) }
                )
            }
        }
    }
}