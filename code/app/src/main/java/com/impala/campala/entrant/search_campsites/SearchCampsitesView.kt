package com.impala.campala.entrant.search_campsites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.impala.campala.ViewObserver

class SearchCampsitesView(val controller: SearchCampsitesController): ViewObserver<SearchCampsitesModel> {
    private var searchString by mutableStateOf("");
    private var minimumOccupantsFilter by mutableIntStateOf(0)
    private var locationFilter by mutableStateOf("");
    private var dateFilter by mutableStateOf("");

    override fun update(model: SearchCampsitesModel) {
        this.searchString = model.searchString
        this.minimumOccupantsFilter = model.minimumOccupantsFilter
        this.locationFilter = model.locationFilter
        this.dateFilter = model.dateFilter
    }

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
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = false,
                    onClick = {},
                    label = { Text("Date", style=MaterialTheme.typography.labelLarge) },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Date Icon") }
                )
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = false,
                    onClick = {},
                    label = { Text("Location", style=MaterialTheme.typography.labelLarge) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Date Icon") }
                )
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = false,
                    onClick = {},
                    label = { Text("Capacity", style=MaterialTheme.typography.labelLarge) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Date Icon") }
                )
            }
        }

    }
}