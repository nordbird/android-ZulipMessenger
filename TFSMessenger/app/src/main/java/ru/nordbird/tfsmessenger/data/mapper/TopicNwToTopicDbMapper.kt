package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.TopicDb
import ru.nordbird.tfsmessenger.data.model.TopicNw

class TopicNwToTopicDbMapper : Mapper<List<TopicNw>, List<TopicDb>> {

    override fun transform(data: List<TopicNw>): List<TopicDb> {
        return data.map { TopicDb(name = it.name) }
    }
}
