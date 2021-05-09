package ru.nordbird.tfsmessenger.ui.topic.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter

const val STREAM_TOPIC_PRESENTER = "Stream"
const val SINGLE_TOPIC_PRESENTER = "Topic"

abstract class TopicPresenter : RxPresenter<TopicView, TopicAction>(TopicView::class.java)