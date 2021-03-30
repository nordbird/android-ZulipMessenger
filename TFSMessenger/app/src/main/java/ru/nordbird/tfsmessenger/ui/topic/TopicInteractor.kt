package ru.nordbird.tfsmessenger.ui.topic

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ru.nordbird.tfsmessenger.data.mapper.MessageToViewTypedMapper
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.model.Resource
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.data.repository.MessageRepository
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

object TopicInteractor {

    private val messageRepository = MessageRepository
    private val messageMapper = MessageToViewTypedMapper()

    private val messages: BehaviorSubject<Resource<List<ViewTyped>>> = BehaviorSubject.create()
    private val compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(loadMessages())
    }

    fun clearDisposable() {
        compositeDisposable.clear()
    }

    fun getMessages() = messages

    fun loadMessages(): Disposable {
        return messageRepository.getMessages()
            .flatMap { resource -> transformMessages(resource) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { messages.onNext(it) }
    }

    fun addMessage(user: User, text: String): Observable<Resource<Message>> = messageRepository.addMessage(user, text).doAfterNext { loadMessages() }

    fun updateReaction(messageId: String, userId: String, reactionCode: Int): Observable<Resource<Message>> =
        messageRepository.updateReaction(messageId, userId, reactionCode).doAfterNext { loadMessages() }

    private fun transformMessages(resource: Resource<List<Message>>): Observable<Resource<List<ViewTyped>>> {
        return Observable.zip(
            Observable.just(resource.status),
            messageMapper.transform(resource.data ?: emptyList())
        ) { status, data -> Resource(status, data) }
    }
}