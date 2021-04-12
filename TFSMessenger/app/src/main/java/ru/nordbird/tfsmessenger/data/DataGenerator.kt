package ru.nordbird.tfsmessenger.data

import io.reactivex.Observable
import io.reactivex.Single
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
        User(1, "Mike", "mike@mail.ru", ""),
        User(2, "Ronald", "ron@bk.ru", ""),
        User(3, "Alex", "alexxxx@rambler.ru", "")
    )

    private val texts = listOf(
        "Я сегодня молодец) все получилось!!!",
        "Надо будет еще посидеть вечером, поработать",
        "Все дела отложу на выходные, лучше погуляю"
    )

    private val streams = listOf(
        Stream(1, "#general"),
        Stream(2, "#Development"),
        Stream(3, "#Design"),
        Stream(4, "#PR"),
        Stream(5, "#general second"),
        Stream(6, "#Development second"),
        Stream(7, "#Design second"),
        Stream(8, "#PR second")
    )

    private val topics = listOf(
        Topic("Testing"),
        Topic("Dev"),
        Topic("Holidays"),
        Topic("Boss"),
        Topic("NewYear"),
        Topic("Monday"),
        Topic("Bob"),
        Topic("Mary")
    )

    private val messages = mutableListOf<Message>()
    private val messagesSubject: BehaviorSubject<List<Message>> = BehaviorSubject.create()

    init {
        messagesSubject.onNext(getRandomMessagesWithError())
    }

    private fun generateReactions(): List<Reaction> {
        val list = mutableListOf<Reaction>()
        val count = (1..5).random()
        (1..count).forEach {
            list.add(Reaction(getReaction(0x1F600 + it), "", authors.random().id))
        }
        return list
    }

    private fun getReaction(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    fun getAllStreams(): Observable<List<Stream>> =
        Observable.fromCallable { getAllStreamsWithError() }

    private fun getAllStreamsWithError(): List<Stream> {
        return if ((0..10).random() < 7) throw RuntimeException("all streams error") else streams
    }

    fun getSubscribedStreams(): Observable<List<Stream>> =
        Observable.fromCallable { getSubscribedStreamsWithError() }

    private fun getSubscribedStreamsWithError(): List<Stream> {
        return if ((0..10).random() < 7) throw RuntimeException("subscribed streams error") else streams.subList(3, 6)
    }

    fun getCurrentUser() = authors[0]

    fun getRandomMessages(): Observable<List<Message>> = messagesSubject

    private fun getRandomMessagesWithError(): List<Message> {
        val date = Date()
        repeat(5) {
            lastId++
            date.add((-1..0).random(), TimeUnits.DAY)
            val author = authors.random()
            val message = Message(
                lastId,
                author.id,
                author.full_name,
                author.avatar_url,
                texts.random(),
                date.time,
                generateReactions(),
                Random.nextBoolean()
            )
            messages.add(message)
        }
        return messages
    }

    fun getRandomIncomingMessage(): Message {
        lastId++
        val author = authors.random()
        return Message(
            lastId,
            author.id,
            author.full_name,
            author.avatar_url,
            texts.random(),
            Date().time,
            mutableListOf(),
            true
        )
    }

    fun addMessage(user: User, text: String): Observable<Message> {
        return makeMessage(user, text).doOnNext {
            messages.add(0, it)
            messages.add(0, getRandomIncomingMessage())
            messagesSubject.onNext(messages)
        }
    }

    private fun makeMessage(user: User, text: String) = Observable.fromCallable { makeMessageWithError(user, text) }

    private fun makeMessageWithError(user: User, text: String): Message {
        return if (((0..10).random() < 4)) {
            throw RuntimeException("add message error")
        } else {
            lastId++
            Message(
                lastId,
                user.id,
                user.full_name,
                user.avatar_url,
                text,
                Date().time,
                mutableListOf(),
                false
            )
        }
    }

    fun getTopics(streamId: String) = Observable.fromArray(topics)

    fun getUsers(): Single<List<User>> =
        Single.fromCallable { getUsersWithError() }

    private fun getUsersWithError(): List<User> {
        return if ((0..10).random() < 8) throw RuntimeException("users error") else authors
    }

    fun updateReaction(messageId: String, userId: String, reactionCode: String): Observable<Message> {
        return Observable.fromArray(messages).flatMap { list ->
//            if ((0..10).random() < 8) return@flatMap Observable.just(Resource.error())

            val message = list.firstOrNull { it.id.toString() == messageId } ?: throw RuntimeException("Error on server")
            val reactions = message.reactions.toMutableList()

            val reaction = reactions.firstOrNull { it.code == reactionCode && it.userId.toString() == userId }
            if (reaction != null) {
                reactions.remove(reaction)
            } else {
                reactions.add(Reaction(reactionCode, reactionCode, userId.toInt()))
            }
            message.reactions = reactions
            return@flatMap Observable.just(message)

        }.doOnNext { message ->
            messagesSubject.onNext(messages)
        }


    }

}