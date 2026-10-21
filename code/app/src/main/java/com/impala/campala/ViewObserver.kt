package com.impala.campala

interface ViewObserver<M> {
    fun update(model: M)
}