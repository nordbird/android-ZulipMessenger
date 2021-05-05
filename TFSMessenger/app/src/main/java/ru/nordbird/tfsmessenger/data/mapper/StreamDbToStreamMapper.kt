package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.mapper.base.Mapper
import ru.nordbird.tfsmessenger.data.model.Stream
import ru.nordbird.tfsmessenger.data.model.StreamDb

class StreamDbToStreamMapper : Mapper<List<StreamDb>, List<Stream>> {

    override fun transform(data: List<StreamDb>): List<Stream> {
        return data.map { Stream(it.id, it.name) }
    }

}