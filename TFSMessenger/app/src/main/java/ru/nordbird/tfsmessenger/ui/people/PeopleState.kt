package ru.nordbird.tfsmessenger.ui.people

import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.ErrorUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserShimmerUi

data class PeopleState(
    val filterQuery: String = "",
    val items: List<ViewTyped> = listOf(UserShimmerUi(), UserShimmerUi(), UserShimmerUi()),
    val error: Throwable? = null,
    val isLoading: Boolean = false,
)

internal fun PeopleState.reduce(peopleAction: PeopleAction): PeopleState {
    return when (peopleAction) {
        PeopleAction.LoadUsers -> copy(
            items = listOf(UserShimmerUi(), UserShimmerUi(), UserShimmerUi()),
            isLoading = true,
            error = null
        )

        is PeopleAction.UsersLoaded -> copy(
            items = peopleAction.items,
            isLoading = false
        )

        is PeopleAction.ErrorLoadUsers -> copy(
            items = listOf(ErrorUi()),
            error = peopleAction.error,
            isLoading = false
        )

        is PeopleAction.SearchUsers -> copy(
            filterQuery = peopleAction.query
        )

        PeopleAction.SearchUsersStop -> copy(
            isLoading = false
        )
    }
}
