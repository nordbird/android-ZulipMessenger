package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.TopicDb
import ru.nordbird.tfsmessenger.data.model.TopicNw

class TopicNwToTopicDbMapper : Mapper<TopicNw, TopicDb> {

    override fun transform(data: TopicNw): TopicDb {
        return TopicDb(name = data.name)
    }
}
