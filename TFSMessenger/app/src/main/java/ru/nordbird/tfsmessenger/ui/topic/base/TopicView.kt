package ru.nordbird.tfsmessenger.ui.topic.base

import ru.nordbird.tfsmessenger.ui.mvi.base.MviView
import ru.nordbird.tfsmessenger.ui.topic.TopicState

interface TopicView : MviView<TopicState, TopicUiEffect>