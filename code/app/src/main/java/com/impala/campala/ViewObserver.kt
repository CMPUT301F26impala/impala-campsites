package com.impala.campala

import androidx.compose.runtime.Composable

/**
 * The View interface for the MVC classes
 */
interface ViewObserver<M> {
    /**
     * Update all the details of this View
     *
     * @param model the model which contains the data to update to
     */
    fun update(model: M)

    /**
     * The actual rendered ui
     */
    // we should add this but then ill have to add override to everything
//    @Composable
//    fun Render(){
//
//    }
}