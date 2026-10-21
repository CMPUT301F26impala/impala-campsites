package com.impala.campala.entrant.search_campsites

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
        // https://developer.android.com/develop/ui/compose/components/search-bar
        SearchBar(
            expanded = false,
            onExpandedChange = {},
            inputField = {
                SearchBarDefaults.InputField(
                    onSearch = {},
                    expanded = false,
                    onExpandedChange = {},
                    query=searchString,
                    onQueryChange = { controller.onSearchBarInputChange(it) }
                )
            }
        ) {}
    }
}