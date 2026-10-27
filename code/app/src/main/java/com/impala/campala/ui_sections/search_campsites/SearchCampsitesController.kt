package com.impala.campala.ui_sections.search_campsites

import com.impala.campala.Controller
import com.impala.campala.model.CampsiteModel
import java.time.LocalDate


/**
 * Controller for the 'Browse Campsites' screen
 *
 * Largely just acts as a middleman to pass data and click actions from the View to the model
 *
 * @param model the SearchCampsitesModel this controller updates
 */
class SearchCampsitesController(val model: SearchCampsitesModel): Controller {
    /**
     * Called in a LaunchedEffect in the View, loads the campsites when the screen is shown
     */
    fun onFirstRender() {
        model.getInitialData()
    }

    /**
     * Called when the entrant types in the search bar
     *
     * @param newValue the full search bar text after the change
     */
    fun onSearchBarInputChange(newValue: String) {
        model.setSearchBarString(newValue);
    }

    /**
     * Called when a new capacity is chosen
     *
     * @param newCapacity the minimum number of people, or null for "any capacity is ok"
     */
    fun onCapacityChange(newCapacity: Int?) {
        model.setMinimumOccupancy(newCapacity)
    }

    /**
     * Called when the entrant picks an option from the location filter
     *
     * @param newLocation the location to filter to, null for any location
     */
    fun onLocationChange(newLocation: String?) {
        model.setLocation(newLocation)
    }

    /**
     * Called when the entrant confirms a date in the date picker
     *
     * @param newDate the selected date, null if none was picked
     */
    fun onDateChange(newDate: LocalDate?) {
        model.setDate(newDate)
    }

    fun onCampsitePress(campsite: CampsiteModel) {
        // TODO: Take the user to that campsite details page
    }
}

