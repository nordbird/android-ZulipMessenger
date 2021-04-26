package ru.nordbird.tfsmessenger.ui.people

import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

sealed class PeopleAction {

    object LoadUsers : PeopleAction()

    data class UsersLoaded(val items: List<UserUi>) : PeopleAction()

    data class ErrorLoadUsers(val error: Throwable) : PeopleAction()

    data class SearchUsers(val query: String) : PeopleAction()

    object SearchUsersStop : PeopleAction()

}