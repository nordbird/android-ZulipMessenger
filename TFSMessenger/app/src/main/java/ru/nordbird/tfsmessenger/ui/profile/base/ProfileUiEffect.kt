package ru.nordbird.tfsmessenger.ui.profile.base

sealed class ProfileUiEffect {

    class LoadUserError(val error: Throwable) : ProfileUiEffect()

}
