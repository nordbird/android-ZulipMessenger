package ru.nordbird.tfsmessenger.ui.profile

import ru.nordbird.tfsmessenger.data.model.Presence
import ru.nordbird.tfsmessenger.ui.profile.base.ProfileAction
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

data class ProfileState(
    val item: UserUi? = null,
    val presence: Presence? = null
)

internal fun ProfileState.reduce(profileAction: ProfileAction): ProfileState {
    return when (profileAction) {
        is ProfileAction.LoadProfile -> copy(
            item = null,
            presence = null
        )

        is ProfileAction.ProfileLoaded -> copy(
            item = profileAction.item
        )


        ProfileAction.LoadProfileStop -> copy(
            item = null
        )

        is ProfileAction.PresenceLoaded -> {
            val newItem = if (item != null) UserUi(item.id, item.name, item.email, item.avatar, profileAction.presence.timestamp_sec) else item
            copy(
                item = newItem,
                presence = profileAction.presence
            )
        }

        ProfileAction.LoadPresenceStop -> this
    }
}
