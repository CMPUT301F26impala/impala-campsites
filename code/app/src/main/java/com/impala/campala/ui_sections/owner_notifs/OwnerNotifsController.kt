package com.impala.campala.ui_sections.owner_notifs

class OwnerNotifsController(val model: OwnerNotifsModel) {
    suspend fun onLoad(){
        model.load()
    }
}