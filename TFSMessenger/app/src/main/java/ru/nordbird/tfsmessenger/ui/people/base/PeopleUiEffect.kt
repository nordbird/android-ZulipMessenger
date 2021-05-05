package ru.nordbird.tfsmessenger.ui.people.base

sealed class PeopleUiEffect {

    class ActionError(val error: Throwable) : PeopleUiEffect()

}
