package ru.nordbird.tfsmessenger.data.mapper

import io.reactivex.Observable
import ru.nordbird.tfsmessenger.data.model.Stream
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi

class StreamToStreamUiMapper : Mapper<List<Stream>, Observable<List<StreamUi>>> {

    override fun transform(data: List<Stream>): Observable<List<StreamUi>> {
        return Observable.just(data.map { StreamUi(it.id, it.name) })
    }

}