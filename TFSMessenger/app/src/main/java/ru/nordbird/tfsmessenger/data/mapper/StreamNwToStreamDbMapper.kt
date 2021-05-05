package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.mapper.base.Mapper
import ru.nordbird.tfsmessenger.data.model.StreamDb
import ru.nordbird.tfsmessenger.data.model.StreamNw

class StreamNwToStreamDbMapper : Mapper<List<StreamNw>, List<StreamDb>> {

    override fun transform(data: List<StreamNw>): List<StreamDb> {
        return data.map { StreamDb(it.id, it.name) }
    }
}
