package ru.nordbird.tfsmessenger.data

import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.model.Reaction
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.extensions.TimeUnits
import ru.nordbird.tfsmessenger.extensions.add
import java.util.*
import kotlin.random.Random

object DataGenerator {

    private var lastId = 0

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
            list.add(Reaction((0x1F600 + it), authors.random().id))
        }
        return list
    }

    fun getCurrentUser() = authors[0]

    fun getRandomMessages(count: Int): List<Message> {
        val list = mutableListOf<Message>()
        val date = Date()
        repeat(count) {
            lastId++
            date.add((-1..0).random(), TimeUnits.DAY)
            val message = Message(
                    "$lastId",
                    authors.random(),
                    texts.random(),
                    Random.nextBoolean(),
                    Date(date.time),
                    Random.nextBoolean(),
                    generateReactions()
            )
            list.add(message)
        }
        return list
    }

    fun getRandomIncomingMessage(): Message {
        lastId++
        return Message(
                "$lastId",
                authors.random(),
                texts.random(),
                true,
                Date(),
                false,
                mutableListOf()
        )
    }

    fun makeMessage(user: User, text: String): Message {
        lastId++
        return Message(
                "$lastId",
                user,
                text,
                false,
                Date(),
                false,
                mutableListOf()
        )
    }
}