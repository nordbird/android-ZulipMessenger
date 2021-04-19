package ru.nordbird.tfsmessenger.ui.mvi.base

interface MviView<State, UiEffect> {

    fun render(state: State)

    fun handleUiEffect(uiEffect: UiEffect)
}

