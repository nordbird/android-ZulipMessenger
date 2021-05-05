package ru.nordbird.tfsmessenger.ui.profile.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter

abstract class ProfilePresenter : RxPresenter<ProfileView, ProfileAction>(ProfileView::class.java)