package ru.nordbird.tfsmessenger.ui.mvi.base.presenter

interface Presenter<View> {

    fun attachView(view: View)

    fun detachView(isFinishing: Boolean)
}