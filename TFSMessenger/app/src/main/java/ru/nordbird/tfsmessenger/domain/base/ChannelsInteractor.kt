package ru.nordbird.tfsmessenger.domain.base

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.UnreadCounter
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi

interface ChannelsInteractor {

    fun loadStreams(): Flowable<List<StreamUi>>

    fun loadTopics(streamId: Int, streamName: String): Flowable<List<TopicUi>>

    fun getTopicUnreadMessageCount(streamName: String, topicName: String): Single<UnreadCounter>

}