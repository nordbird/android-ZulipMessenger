package ru.nordbird.tfsmessenger.ui.topic.base

import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter

const val STREAM_TOPIC_PRESENTER = "Stream"
const val SINGLE_TOPIC_PRESENTER = "Topic"
const val EDIT_MESSAGE_PRESENTER = "Message"

abstract class TopicPresenter : RxPresenter<TopicView, TopicAction>(TopicView::class.java)