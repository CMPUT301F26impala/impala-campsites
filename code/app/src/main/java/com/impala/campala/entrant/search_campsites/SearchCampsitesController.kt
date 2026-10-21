package com.impala.campala.entrant.search_campsites

import com.impala.campala.Controller


class SearchCampsitesController(val model: SearchCampsitesModel): Controller {
    fun onSearchBarInputChange(newValue: String) {
        model.setSearchBarString(newValue);
    }
}

