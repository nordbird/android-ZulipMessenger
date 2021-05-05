package ru.nordbird.tfsmessenger.ui.people.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter

abstract class PeoplePresenter : RxPresenter<PeopleView, PeopleAction>(PeopleView::class.java)