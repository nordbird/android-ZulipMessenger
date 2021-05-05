package ru.nordbird.tfsmessenger.ui.channels.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter

abstract class ChannelsPresenter : RxPresenter<ChannelsView, ChannelsAction>(ChannelsView::class.java)