package ru.nordbird.tfsmessenger.ui.mvi.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.Presenter

interface MviViewCallback<View, P : Presenter<View>> {

    fun getPresenter(): P

    fun getMviView(): View

}