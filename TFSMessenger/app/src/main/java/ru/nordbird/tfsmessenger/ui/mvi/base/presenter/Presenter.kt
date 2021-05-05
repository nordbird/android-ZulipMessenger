package ru.nordbird.tfsmessenger.ui.mvi.base.presenter

import io.reactivex.functions.Consumer

interface Presenter<View, Action> {

    fun attachView(view: View)

    fun detachView(isFinishing: Boolean)

    val input: Consumer<Action> get() = error("provide action input $this")
}