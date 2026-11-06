package com.impala.campala

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
}