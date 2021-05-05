package ru.nordbird.tfsmessenger.ui.mvi.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.Presenter

interface MviViewCallback<View, Action, P : Presenter<View, Action>> {

    fun getPresenter(): P

    fun getMviView(): View

}