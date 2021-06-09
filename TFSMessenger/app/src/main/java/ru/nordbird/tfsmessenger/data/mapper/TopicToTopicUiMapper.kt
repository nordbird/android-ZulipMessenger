package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.mapper.base.Mapper
import ru.nordbird.tfsmessenger.data.model.Topic
import ru.nordbird.tfsmessenger.data.model.getColorType
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi

class TopicToTopicUiMapper : Mapper<List<Topic>, List<TopicUi>> {

    override fun transform(data: List<Topic>): List<TopicUi> {
        return data.map { TopicUi(it.name, it.streamName, getColorType(it.name), 0) }
    }

}