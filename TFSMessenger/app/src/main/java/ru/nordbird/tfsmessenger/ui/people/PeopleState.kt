package ru.nordbird.tfsmessenger.ui.people

import ru.nordbird.tfsmessenger.data.model.Presence
import ru.nordbird.tfsmessenger.ui.people.base.PeopleAction
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.ErrorUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserShimmerUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

data class PeopleState(
    val filterQuery: String = "",
    val items: List<ViewTyped> = listOf(UserShimmerUi(), UserShimmerUi(), UserShimmerUi()),
    val users: List<UserUi> = emptyList(),
    val presences: List<Presence> = emptyList(),
    val needScroll: Boolean = false
)

internal fun PeopleState.reduce(peopleAction: PeopleAction): PeopleState {
    return when (peopleAction) {
        PeopleAction.LoadUsers -> {
            val list = if (users.isEmpty()) listOf(UserShimmerUi(), UserShimmerUi(), UserShimmerUi()) else items
            copy(
                items = list,
                needScroll = false
            )
        }

        is PeopleAction.UsersLoaded -> {
            copy(
                users = peopleAction.users,
                items = combineItems(peopleAction.users, presences, filterQuery)
            )
        }

        PeopleAction.LoadUsersStop -> {
            val list = if (users.isEmpty()) listOf(ErrorUi()) else items
            copy(
                items = list
            )
        }

        is PeopleAction.SearchUsers -> copy(
            filterQuery = peopleAction.query
        )

        PeopleAction.UsersFiltered -> {
            copy(
                items = combineItems(users, presences, filterQuery),
                needScroll = true
            )
        }

        PeopleAction.FilterUsersStop -> this

        is PeopleAction.UserPresenceLoaded -> {
            val list = listOf(peopleAction.presence) + presences
            val presenceList = list.distinctBy { it.userId }
            copy(
                presences = presenceList,
                items = combineItems(users, presenceList, filterQuery),
                needScroll = false,
            )
        }

        PeopleAction.LoadUserPresenceStop -> this
    }
}

private fun combineItems(users: List<UserUi>, presences: List<Presence>, filterQuery: String): List<ViewTyped> {
    return users.filter { it.name.contains(filterQuery, true) }.sortedBy { it.name }.map { user ->
        val presence = presences.firstOrNull { it.userId == user.id }
        if (presence != null) UserUi(user.id, user.name, user.email, user.avatar, presence.timestamp_sec) else user
    }
}