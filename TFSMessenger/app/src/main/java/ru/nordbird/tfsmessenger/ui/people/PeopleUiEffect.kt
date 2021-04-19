package ru.nordbird.tfsmessenger.ui.people

sealed class PeopleUiEffect {

    class SearchUsersError(val error: Throwable) : PeopleUiEffect()

}
