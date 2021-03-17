package ru.nordbird.tfsmessenger.data

import ru.nordbird.tfsmessenger.data.model.BaseMessage
import ru.nordbird.tfsmessenger.data.model.MessageType
import ru.nordbird.tfsmessenger.data.model.Reaction
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.extensions.TimeUnits
import ru.nordbird.tfsmessenger.extensions.add
import java.util.*
import kotlin.random.Random

object DataGenerator {

    private val authors = listOf(
        User("1", "Mike"),
        User("2", "Ronald"),
        User("3", "Alex")
    )

    private val texts = listOf(
        "Я сегодня молодец) все получилось!!!",
        "Надо будет еще посидеть вечером, поработать",
        "Все дела отложу на выходные, лучше погуляю"
    )

    private fun generateReactions(): List<Reaction> {
        val list = mutableListOf<Reaction>()
        val count = (1..5).random()
        (1..count).forEach {
            list.add(Reaction((0x1F600 + it), mutableListOf(authors.random().id)))
        }
        return list
    }

    fun getCurrentUser() = authors[0]

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
                Random.nextBoolean(),
                generateReactions()
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
            true,
            mutableListOf()
        )
    }

}