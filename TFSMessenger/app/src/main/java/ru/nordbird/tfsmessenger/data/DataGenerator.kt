package ru.nordbird.tfsmessenger.data

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import ru.nordbird.tfsmessenger.data.model.*
import ru.nordbird.tfsmessenger.extensions.TimeUnits
import ru.nordbird.tfsmessenger.extensions.add
import java.util.*
import java.util.concurrent.TimeUnit
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

    private val messages = mutableListOf<Message>()
    private val messagesSubject: BehaviorSubject<Resource<List<Message>>> = BehaviorSubject.create()

    init {
        messagesSubject.onNext(getRandomMessagesWithError())
    }

    private fun generateReactions(): List<Reaction> {
        val list = mutableListOf<Reaction>()
        val count = (1..5).random()
        (1..count).forEach {
            list.add(Reaction((0x1F600 + it), authors.random().id))
        }
        return list
    }

    fun getAllStreams(): Observable<Resource<List<Stream>>> =
        Observable.fromCallable { getAllStreamsWithError() }.delay(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())

    private fun getAllStreamsWithError(): Resource<List<Stream>> {
        return if ((0..10).random() < 7) Resource.error() else Resource.success(streams)
    }

    fun getSubscribedStreams(): Observable<Resource<List<Stream>>> =
        Observable.fromCallable { getSubscribedStreamsWithError() }.delay(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())

    private fun getSubscribedStreamsWithError(): Resource<List<Stream>> {
        return if ((0..10).random() < 7) Resource.error() else Resource.success(streams.subList(3, 6))
    }

    fun getTopics(streamId: String) = Observable.fromArray(topics.filter { it.streamId == streamId })

    fun getCurrentUser() = authors[0]

    fun getRandomMessages(): Observable<Resource<List<Message>>> = messagesSubject.delay(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())

    private fun getRandomMessagesWithError(): Resource<List<Message>> {
        val date = Date()
        repeat(5) {
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
            messages.add(message)
        }
        return Resource.success(messages)
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

    fun addMessage(user: User, text: String): Observable<Resource<Message>> {
        return makeMessage(user, text).doOnNext {
            if (it.data == null) {
                throw RuntimeException("Error on server")
            } else {
                messages.add(0, it.data)
                messages.add(0, getRandomIncomingMessage())
                messagesSubject.onNext(Resource.success(messages))
            }
        }
    }

    private fun makeMessage(user: User, text: String) = Observable.fromCallable { makeMessageWithError(user, text) }

    private fun makeMessageWithError(user: User, text: String): Resource<Message> {
        return if (((0..10).random() < 4)) {
            Resource.error()
        } else {
            lastId++
            Resource.success(
                Message(
                    "$lastId",
                    user,
                    text,
                    false,
                    Date(),
                    false,
                    mutableListOf()
                )
            )
        }
    }

    fun getUsers(): Observable<Resource<List<User>>> =
        Observable.fromCallable { getUsersWithError() }.delay(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())

    private fun getUsersWithError(): Resource<List<User>> {
        return if ((0..10).random() < 8) Resource.error() else Resource.success(authors)
    }

    fun updateReaction(messageId: String, userId: String, reactionCode: Int): Observable<Resource<Message>> {
        return Observable.fromArray(messages).flatMap { list ->
            if ((0..10).random() < 8) return@flatMap Observable.just(Resource.error())

            val message = list.firstOrNull { it.id == messageId } ?: return@flatMap Observable.just(Resource.error())
            val reactions = message.reactions.toMutableList()

            val reaction = reactions.firstOrNull { it.code == reactionCode && it.userId == userId }
            if (reaction != null) {
                reactions.remove(reaction)
            } else {
                reactions.add(Reaction(reactionCode, userId))
            }
            message.reactions = reactions
            return@flatMap Observable.just(Resource.success(message))

        }.doOnNext { resource ->
            if (resource.data == null) {
                throw RuntimeException("Error on server")
            } else {
                messagesSubject.onNext(Resource.success(messages))
            }
        }


    }
}