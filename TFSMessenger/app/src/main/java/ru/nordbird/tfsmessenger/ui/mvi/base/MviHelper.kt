package ru.nordbird.tfsmessenger.ui.mvi.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.Presenter

class MviHelper<View, Action, P : Presenter<View, Action>>(
    private val callback: MviViewCallback<View, Action, P>
) {

    private lateinit var presenter: Presenter<View, Action>

    fun create() {
        presenter = callback.getPresenter()
        presenter.attachView(callback.getMviView())
    }

    fun destroy(isFinishing: Boolean) {
        presenter.detachView(isFinishing)
    }
}