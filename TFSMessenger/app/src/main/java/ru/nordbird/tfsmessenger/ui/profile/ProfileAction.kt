package ru.nordbird.tfsmessenger.ui.profile

import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

sealed class ProfileAction {

    data class LoadProfile(val userId: Int) : ProfileAction()

    data class ProfileLoaded(val item: UserUi) : ProfileAction()

    data class ErrorLoadProfile(val error: Throwable) : ProfileAction()

}