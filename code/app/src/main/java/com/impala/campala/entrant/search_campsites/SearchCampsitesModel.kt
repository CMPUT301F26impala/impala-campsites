package com.impala.campala.entrant.search_campsites

import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

class SearchCampsitesModel(val repo: CampsiteRepository, val scope: CoroutineScope): ObservableModel<SearchCampsitesModel>() {
    var searchString = "";
    var minimumOccupantsFilter: Int? = null;
    var locationFilter: String? = null;
    var dateFilter: LocalDate? = null;

    var results: List<CampsiteModel> = emptyList()
        private set

    private var resultsJob: Job? = null
    fun updateResults() {
        // If you type, technically this would fire on every keystroke
        // We need to cancel the previous one before starting a new one, that way we don't have a bunch going at once
        resultsJob?.cancel()
        resultsJob = this.scope.launch {
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