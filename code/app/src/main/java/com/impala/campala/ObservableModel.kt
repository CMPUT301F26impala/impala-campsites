package com.impala.campala

abstract class ObservableModel<M> {
    private val observers = mutableListOf<ViewObserver<M>>()

    fun addObserver(o: ViewObserver<M>) {
        observers.add(o)
    }
    fun removeObserver(o: ViewObserver<M> ) {
        observers.remove(o)
    }

    protected fun notifyObservers(model: M) {
        for (observer in observers) {
            observer.update(model)
        }
    }
}