package com.impala.campala.ui_sections.form_campsites

import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.CoroutineScope

class CampsiteFormModel(
    val repo: CampsiteRepository,
    val scope: CoroutineScope,
    val campsite: CampsiteModel? = null
): ObservableModel<CampsiteFormModel>() {

    var title = if (campsite == null) "" else campsite.name
    var location = if (campsite == null) "" else campsite.location
    var description = if (campsite == null) "" else campsite.description
    var maximumOccupancy = if (campsite == null) "" else campsite.maxOccupancy.toString()
    var tags = if (campsite == null) mutableListOf<String>() else campsite.tags.toMutableList()

    fun setTitle(newTitle: String) {
        title = newTitle
        notifyObservers(this)
    }
    fun setLocation(newLocation: String) {
        location = newLocation
        notifyObservers(this)
    }

    fun setDescription(newDescription: String) {
        description = newDescription
        notifyObservers(this)
    }

    fun setMaximumOccupancy(newOccupancy: String) {
        maximumOccupancy = newOccupancy
        notifyObservers(this)
    }

    fun addTag(newTag: String) {
        val trimmedTag = newTag.trim()

        if (trimmedTag.isNotEmpty()) {
            tags.add(trimmedTag)
            notifyObservers(this)
        }
    }

    fun removeTag(tag: String) {
        tags.remove(tag)
        notifyObservers(this)
    }
}
