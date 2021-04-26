package ru.nordbird.tfsmessenger.ui.profile

import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

data class ProfileState(
    val userId: Int = 0,
    val item: UserUi? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null
)

internal fun ProfileState.reduce(profileAction: ProfileAction): ProfileState {
    return when (profileAction) {
        is ProfileAction.LoadProfile -> copy(
            userId = profileAction.userId,
            item = null,
            isLoading = true,
            error = null
        )
        is ProfileAction.ProfileLoaded -> {
            copy(
                item = profileAction.item,
                isLoading = false
            )
        }
        is ProfileAction.ErrorLoadProfile -> copy(
            item = null,
            isLoading = false,
            error = profileAction.error
        )
    }
}
