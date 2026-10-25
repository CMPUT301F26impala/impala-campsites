package com.impala.campala.entrant.search_campsites

import androidx.annotation.Nullable
import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class SearchCampsitesModel(val repo: CampsiteRepository, val scope: CoroutineScope): ObservableModel<SearchCampsitesModel>() {
    var searchString = "";
    var minimumOccupantsFilter: Int? = null;
    var locationFilter: String? = null;
    var dateFilter: LocalDate? = null;

    var results: List<CampsiteModel> = mutableListOf()

    private fun updateResults() {
        this.scope.launch {
            results = repo.getFilteredCampsites(searchString, locationFilter, null, minimumOccupantsFilter, null)
            notifyObservers(this@SearchCampsitesModel)
        }
    }

    fun setSearchBarString(value: String) {
        searchString = value;
        this.notifyObservers(this)
        this.updateResults()
    }
    fun setMinimumOccupancy(value: Int?) {
        if (value == 0) {
            minimumOccupantsFilter = null;
        } else {
            minimumOccupantsFilter = value;
        }
        this.notifyObservers(this)
        this.updateResults()

    }
    fun setLocation(value: String?) {
        locationFilter = value;
        this.notifyObservers(this)
        this.updateResults()

    }
    fun setDate(value: LocalDate?) {
        dateFilter = value;
        this.notifyObservers(this)
        this.updateResults()
    }
}