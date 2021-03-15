package ru.nordbird.tfsmessenger.data

import ru.nordbird.tfsmessenger.data.model.BaseMessage
import ru.nordbird.tfsmessenger.data.model.MessageType
import ru.nordbird.tfsmessenger.extensions.TimeUnits
import ru.nordbird.tfsmessenger.extensions.add
import java.util.*
import kotlin.random.Random

object DataGenerator {

    private val authors = listOf("Christina Gonzalez", "Ronald Miller")

    private val texts = listOf(
            "Я сегодня молодец) все получилось!!!",
            "Надо будет еще посидеть вечером, пработать",
            "Все дела отложу на выходные, лучше погуляю"
    )

    fun getRandomMessages(count: Int): List<BaseMessage> {
        val list = mutableListOf<BaseMessage>()
        val date = Date()
        (1..count).forEach {
            date.add((-1..0).random(), TimeUnits.DAY)
            val message = BaseMessage.makeMessage(
                    authors.random(),
                    Date(date.time),
                    MessageType.TEXT,
                    texts.random(),
                    Random.nextBoolean()

            )
            list.add(message)
        }
        return list
    }

    fun getRandomIncomingMessage(): BaseMessage {
        return BaseMessage.makeMessage(
                authors.random(),
                Date(),
                MessageType.TEXT,
                texts.random(),
                true
        )
    }

}