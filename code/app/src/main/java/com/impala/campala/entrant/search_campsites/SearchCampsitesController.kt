package com.impala.campala.entrant.search_campsites

import com.impala.campala.Controller
import java.time.LocalDate


class SearchCampsitesController(val model: SearchCampsitesModel): Controller {
    fun onSearchBarInputChange(newValue: String) {
        model.setSearchBarString(newValue);
    }
    fun onCapacityChange(newCapacity: Int?) {
        model.setMinimumOccupancy(newCapacity)
    }
    fun onLocationChange(newLocation: String?) {
        model.setLocation(newLocation)
    }
    fun onDateChange(newDate: LocalDate?) {
        model.setDate(newDate)
    }
}

