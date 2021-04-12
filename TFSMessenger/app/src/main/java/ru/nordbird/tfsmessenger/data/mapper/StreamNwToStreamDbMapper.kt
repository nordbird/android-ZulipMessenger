package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.StreamDb
import ru.nordbird.tfsmessenger.data.model.StreamNw

class StreamNwToStreamDbMapper : Mapper<StreamNw, StreamDb> {

    override fun transform(data: StreamNw): StreamDb {
        return StreamDb(data.id, data.name)
    }
}
