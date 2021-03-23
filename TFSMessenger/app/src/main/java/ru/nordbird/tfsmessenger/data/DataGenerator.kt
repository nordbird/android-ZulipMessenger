package ru.nordbird.tfsmessenger.data

import ru.nordbird.tfsmessenger.data.model.*
import ru.nordbird.tfsmessenger.extensions.TimeUnits
import ru.nordbird.tfsmessenger.extensions.add
import java.util.*
import kotlin.random.Random

object DataGenerator {

    private var lastId = 0

    private val authors = listOf(
        User("1", "Mike", "mike@mail.ru", true),
        User("2", "Ronald", "ron@bk.ru", false),
        User("3", "Alex", "alexxxx@rambler.ru", true)
    )

    private val texts = listOf(
        "Я сегодня молодец) все получилось!!!",
        "Надо будет еще посидеть вечером, поработать",
        "Все дела отложу на выходные, лучше погуляю"
    )

    private val streams = listOf(
        Stream("1", "#general"),
        Stream("2", "#Development"),
        Stream("3", "#Design"),
        Stream("4", "#PR"),
        Stream("5", "#general second"),
        Stream("6", "#Development second"),
        Stream("7", "#Design second"),
        Stream("8", "#PR second")
    )

    private val topics = listOf(
        Topic("1", "Testing"),
        Topic("1", "Dev"),
        Topic("2", "Holidays"),
        Topic("2", "Boss"),
        Topic("2", "NewYear"),
        Topic("4", "Monday"),
        Topic("6", "Bob"),
        Topic("6", "Mary")
    )

    private fun generateReactions(): List<Reaction> {
        val list = mutableListOf<Reaction>()
        val count = (1..5).random()
        (1..count).forEach {
            list.add(Reaction((0x1F600 + it), authors.random().id))
        }
        return list
    }

    fun getAllStreams() = streams

    fun getSubscribedStreams() = streams.subList(3, 6)

    fun getTopics(streamId: String) = topics.filter { it.streamId == streamId }

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

    fun getUsers() = authors
}