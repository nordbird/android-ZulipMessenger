package ru.nordbird.tfsmessenger.data.mapper

import android.graphics.Color
import ru.nordbird.tfsmessenger.data.mapper.base.Mapper
import ru.nordbird.tfsmessenger.data.model.Topic
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi

class TopicToTopicUiMapper : Mapper<List<Topic>, List<TopicUi>> {

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

    override fun transform(data: List<Topic>): List<TopicUi> {
        return data.map { TopicUi(it.name, it.streamName, getColor(it.name), 0) }
    }

    private fun getColor(name: String): Int {
        val index = name.sumBy { it.toInt() } % topicColors.size
        return topicColors[index]
    }

}