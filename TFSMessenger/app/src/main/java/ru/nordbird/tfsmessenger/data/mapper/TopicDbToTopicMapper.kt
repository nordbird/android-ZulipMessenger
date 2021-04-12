package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.Topic
import ru.nordbird.tfsmessenger.data.model.TopicDb

class TopicDbToTopicMapper : Mapper<List<TopicDb>, List<Topic>> {

    override fun transform(data: List<TopicDb>): List<Topic> {
        return data.map { Topic(it.name, it.streamId) }
    }
}
