package ru.nordbird.tfsmessenger.ui.channels.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter

const val STREAMS_CHANNELS_PRESENTER = "Streams"
const val SUBSCRIPTIONS_CHANNELS_PRESENTER = "Subscriptions"

abstract class ChannelsPresenter : RxPresenter<ChannelsView, ChannelsAction>(ChannelsView::class.java)