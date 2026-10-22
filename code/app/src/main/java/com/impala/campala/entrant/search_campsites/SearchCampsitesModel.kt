package com.impala.campala.entrant.search_campsites

import androidx.annotation.Nullable
import com.impala.campala.ObservableModel
import java.time.LocalDate

class SearchCampsitesModel: ObservableModel<SearchCampsitesModel>() {
    var searchString = "";
    var minimumOccupantsFilter: Int? = null;
    var locationFilter: String? = null;
    var dateFilter: LocalDate? = null;

    fun setSearchBarString(value: String) {
        searchString = value;
        this.notifyObservers(this)
    }
    fun setMinimumOccupancy(value: Int?) {
        if (value == 0) {
            minimumOccupantsFilter = null;
        } else {
            minimumOccupantsFilter = value;
        }
        this.notifyObservers(this)
    }
    fun setLocation(value: String?) {
        locationFilter = value;
        this.notifyObservers(this)
    }
    fun setDate(value: LocalDate?) {
        dateFilter = value;
        this.notifyObservers(this)
    }
}