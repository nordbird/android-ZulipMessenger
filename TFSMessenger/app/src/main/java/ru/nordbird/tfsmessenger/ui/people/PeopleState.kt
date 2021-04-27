package ru.nordbird.tfsmessenger.ui.people

import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.ErrorUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserShimmerUi

data class PeopleState(
    val filterQuery: String = "",
    val items: List<ViewTyped> = emptyList(),
    val error: Throwable? = null,
    val needScroll: Boolean = false
)

internal fun PeopleState.reduce(peopleAction: PeopleAction): PeopleState {
    return when (peopleAction) {
        PeopleAction.LoadUsers -> {
            val list = if (items.isEmpty()) listOf(UserShimmerUi(), UserShimmerUi(), UserShimmerUi()) else items
            copy(
                items = list,
                error = null,
                needScroll = false
            )
        }

        is PeopleAction.UsersLoaded -> {
            val list = peopleAction.users.filter { it.name.contains(filterQuery, true) }
            copy(
                items = list,
                error = null
            )
        }

        is PeopleAction.ErrorLoadUsers -> {
            val list = if (items.filterNot { it is UserShimmerUi }.isEmpty()) listOf(ErrorUi()) else items
            copy(
                items = list,
                error = peopleAction.error
            )
        }

        is PeopleAction.SearchUsers -> copy(
            filterQuery = peopleAction.query
        )

        is PeopleAction.UsersFound -> {
            copy(
                items = peopleAction.users,
                error = null,
                needScroll = true
            )
        }

        PeopleAction.SearchUsersStop -> this
    }
}
