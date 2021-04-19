package ru.nordbird.tfsmessenger.ui.mvi.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.Presenter

class MviHelper<View, P : Presenter<View>>(
    private val callback: MviViewCallback<View, P>
) {

    private lateinit var presenter: Presenter<View>

    fun create() {
        presenter = callback.getPresenter()
        presenter.attachView(callback.getMviView())
    }

    fun destroy(isFinishing: Boolean) {
        presenter.detachView(isFinishing)
    }
}