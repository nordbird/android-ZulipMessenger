package ru.nordbird.tfsmessenger.ui.people

import ru.nordbird.tfsmessenger.data.model.Presence
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

sealed class PeopleAction {

    object LoadUsers : PeopleAction()

    data class UsersLoaded(val users: List<UserUi>) : PeopleAction()

    object LoadUsersStop : PeopleAction()

    data class SearchUsers(val query: String) : PeopleAction()

    object UsersFiltered : PeopleAction()

    object FilterUsersStop : PeopleAction()

    data class UserPresenceLoaded(val presence: Presence) : PeopleAction()

    object LoadUserPresenceStop : PeopleAction()

}