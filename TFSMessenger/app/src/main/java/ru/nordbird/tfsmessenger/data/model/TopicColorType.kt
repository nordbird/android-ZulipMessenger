package ru.nordbird.tfsmessenger.data.model

import ru.nordbird.tfsmessenger.R

enum class TopicColorType(
    val style: Int,
    val color: Int
) {
    TOPIC1(R.style.Topic1Style, R.color.topic1_color),
    TOPIC2(R.style.Topic2Style, R.color.topic2_color),
    TOPIC3(R.style.Topic3Style, R.color.topic3_color),
    TOPIC4(R.style.Topic4Style, R.color.topic4_color),
    TOPIC5(R.style.Topic5Style, R.color.topic5_color),
    TOPIC6(R.style.Topic6Style, R.color.topic6_color),
    TOPIC7(R.style.Topic7Style, R.color.topic7_color),
    TOPIC8(R.style.Topic8Style, R.color.topic8_color)
}

fun getColorType(name: String): TopicColorType {
    val index = name.sumBy { it.toInt() } % TopicColorType.values().size
    return TopicColorType.values()[index]
}