package ru.nordbird.tfsmessenger.ui.profile

sealed class ProfileUiEffect {

    class LoadUserError(val error: Throwable) : ProfileUiEffect()

}
