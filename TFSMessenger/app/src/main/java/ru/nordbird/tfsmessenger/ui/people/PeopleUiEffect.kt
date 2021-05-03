package ru.nordbird.tfsmessenger.ui.people

sealed class PeopleUiEffect {

    class ActionError(val error: Throwable) : PeopleUiEffect()

}
