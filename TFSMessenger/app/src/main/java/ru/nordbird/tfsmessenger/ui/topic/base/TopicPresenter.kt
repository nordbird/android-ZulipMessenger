package ru.nordbird.tfsmessenger.ui.topic.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter

abstract class TopicPresenter : RxPresenter<TopicView, TopicAction>(TopicView::class.java)