package com.impala.campala.entrant.search_campsites

import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Model for the 'Browse Campsites' screen
 *
 * This tracks all the filter state (the search string and the filter values)
 * And it also has a results List, which is a List of campsites that actaully match those filters
 *
 * @param repo the CampsiteRepository to fetch campsites from
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property searchString text typed into the search bar
 * @property minimumOccupantsFilter the minimum capacity a user wants, or null for "any is fine"
 * @property locationFilter only show campsites at this location, null for any
 * @property dateFilter the date the entrant wants to camp on, null for any, CURRENTLY NOT IMPLEMENTED
 * @property results campsites matching the current filters
 */
class SearchCampsitesModel(val repo: CampsiteRepository, val scope: CoroutineScope): ObservableModel<SearchCampsitesModel>() {
    var searchString = "";
    var minimumOccupantsFilter: Int? = null;
    var locationFilter: String? = null;
    var dateFilter: LocalDate? = null;

    var results: List<CampsiteModel> = emptyList()
        private set

    private var resultsJob: Job? = null

    private fun updateResults() {
        // If you type, technically this would fire on every keystroke
        // We need to cancel the previous one before starting a new one, that way we don't have a bunch going at once
        resultsJob?.cancel()
        resultsJob = this.scope.launch {
            results = repo.getFilteredCampsites(searchString, locationFilter, null, minimumOccupantsFilter, null)
            notifyObservers(this@SearchCampsitesModel)
        }
    }

    /**
     * Sets the search bar text and updates results
     *
     * @param value the new search bar text
     */
    fun setSearchBarString(value: String) {
        searchString = value;
        this.notifyObservers(this)
        this.updateResults()
    }

    /**
     * Sets the minimum number of people a campsite has to fit, and updates results
     *
     * @param value the minimum capacity, 0 or null to show campsites of any capacity
     */
    fun setMinimumOccupancy(value: Int?) {
        if (value == 0) {
            minimumOccupantsFilter = null;
        } else {
            minimumOccupantsFilter = value;
        }
        this.notifyObservers(this)
        this.updateResults()

    }

    /**
     * Sets the location filter and updates results
     *
     * @param value the location to filter to, null to show every location
     */
    fun setLocation(value: String?) {
        locationFilter = value;
        this.notifyObservers(this)
        this.updateResults()

    }

    /**
     * Sets the date filter and updates results
     *
     * The date doesn't actually filter anything yet
     *
     * @param value the date to filter to, null for any date
     */
    fun setDate(value: LocalDate?) {
        dateFilter = value;
        this.notifyObservers(this)
        this.updateResults()
    }
}