package ru.nordbird.tfsmessenger.data.mapper

import android.graphics.Color
import io.reactivex.Observable
import ru.nordbird.tfsmessenger.data.model.Topic
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi

class TopicToTopicUiMapper : Mapper<List<Topic>, Observable<List<TopicUi>>> {

    private val topicColors = arrayOf(
        Color.parseColor("#7BC862"),
        Color.parseColor("#E17076"),
        Color.parseColor("#FAA774"),
        Color.parseColor("#6EC9CB"),
        Color.parseColor("#65AADD"),
        Color.parseColor("#A695E7"),
        Color.parseColor("#EE7AAE"),
        Color.parseColor("#2196F3")
    )

    override fun transform(data: List<Topic>): Observable<List<TopicUi>> {
        return Observable.fromArray(data.map { TopicUi(it.streamId, it.name, getColor(it), 0) })
    }

    private fun getColor(it: Topic): Int {
        val index = if (it.name.isEmpty()) 0 else it.name[0].toByte() % topicColors.size
        return topicColors[index]
    }

}