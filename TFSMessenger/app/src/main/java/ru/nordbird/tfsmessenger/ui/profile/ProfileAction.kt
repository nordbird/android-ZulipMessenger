package ru.nordbird.tfsmessenger.ui.profile

import ru.nordbird.tfsmessenger.data.model.Presence
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

sealed class ProfileAction {

    data class LoadProfile(val userId: Int) : ProfileAction()

    data class ProfileLoaded(val item: UserUi) : ProfileAction()

    object LoadProfileStop : ProfileAction()

    data class PresenceLoaded(val presence: Presence): ProfileAction()

    object LoadPresenceStop : ProfileAction()
}