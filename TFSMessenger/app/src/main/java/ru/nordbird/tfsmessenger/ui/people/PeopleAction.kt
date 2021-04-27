package ru.nordbird.tfsmessenger.ui.people

import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

sealed class PeopleAction {

    object LoadUsers : PeopleAction()

    data class UsersLoaded(val users: List<UserUi>) : PeopleAction()

    data class ErrorLoadUsers(val error: Throwable) : PeopleAction()

    data class SearchUsers(val query: String) : PeopleAction()

    data class UsersFound(val users: List<UserUi>) : PeopleAction()

    object SearchUsersStop : PeopleAction()

}