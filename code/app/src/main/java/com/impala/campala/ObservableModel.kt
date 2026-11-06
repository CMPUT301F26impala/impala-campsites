package com.impala.campala

/**
 * This is the model interface for MVC classes
 *
 * @property observers a list of Views for the model
 */
abstract class ObservableModel<M> {
    private val observers = mutableListOf<ViewObserver<M>>()

    /**
     * Add a View to the observers list
     *
     * @param o The View to be added
     */
    fun addObserver(o: ViewObserver<M>) {
        observers.add(o)
    }
    /**
     * Remove a View from the observers list
     *
     * @param o The View to be removed
     */
    fun removeObserver(o: ViewObserver<M> ) {
        observers.remove(o)
    }

    /**
     * Call the update function on all observer Views
     *
     * @param model the model to update all the Views on
     */
    protected fun notifyObservers(model: M) {
        for (observer in observers) {
            observer.update(model)
        }
    }
}