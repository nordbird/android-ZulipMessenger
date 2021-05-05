package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.mapper.base.Mapper
import ru.nordbird.tfsmessenger.data.model.Stream
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi

class StreamToStreamUiMapper : Mapper<List<Stream>, List<StreamUi>> {

    override fun transform(data: List<Stream>): List<StreamUi> {
        return data.map { StreamUi(it.id, it.name) }
    }

}