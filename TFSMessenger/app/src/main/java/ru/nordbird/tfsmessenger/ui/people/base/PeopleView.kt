package ru.nordbird.tfsmessenger.ui.people.base

import ru.nordbird.tfsmessenger.ui.mvi.base.MviView
import ru.nordbird.tfsmessenger.ui.people.PeopleState

interface PeopleView : MviView<PeopleState, PeopleUiEffect>