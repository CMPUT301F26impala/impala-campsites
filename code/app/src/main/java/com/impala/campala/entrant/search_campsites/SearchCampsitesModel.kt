package com.impala.campala.entrant.search_campsites

import com.impala.campala.ObservableModel

class SearchCampsitesModel: ObservableModel<SearchCampsitesModel>() {
    var searchString = "";
    var minimumOccupantsFilter = 0;
    var locationFilter = "";
    var dateFilter = "";

    fun setSearchBarString(value: String) {
        searchString = value;
        this.notifyObservers(this)
    }
    fun setMinimumOccupancy(value: Int) {
        minimumOccupantsFilter = value;
        this.notifyObservers(this)
    }
    fun setLocation(value: String) {
        locationFilter = value;
        this.notifyObservers(this)
    }
    fun setDate(value: String) {
        dateFilter = value;
        this.notifyObservers(this)
    }
}