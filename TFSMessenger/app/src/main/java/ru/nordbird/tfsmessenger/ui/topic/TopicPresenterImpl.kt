package ru.nordbird.tfsmessenger.ui.topic

import com.freeletics.rxredux.SideEffect
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.data.repository.base.EventRepository
import ru.nordbird.tfsmessenger.data.repository.base.TopicRepository
import ru.nordbird.tfsmessenger.domain.base.TopicInteractor
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import ru.nordbird.tfsmessenger.ui.topic.base.TopicAction
import ru.nordbird.tfsmessenger.ui.topic.base.TopicPresenter
import ru.nordbird.tfsmessenger.ui.topic.base.TopicUiEffect
import ru.nordbird.tfsmessenger.ui.topic.base.TopicView
import java.io.InputStream

internal typealias TopicSideEffect = SideEffect<TopicState, out TopicAction>

class TopicPresenterImpl(
    private val topicInteractor: TopicInteractor,
    private val eventRepository: EventRepository,
    private val topicRepository: TopicRepository
) : TopicPresenter() {

    override val input: Consumer<TopicAction> get() = inputRelay

    private val inputRelay: PublishRelay<TopicAction> = PublishRelay.create()
    private val uiEffectsRelay: PublishRelay<TopicUiEffect> = PublishRelay.create()
    private val uiEffectsInput: Observable<TopicUiEffect> get() = uiEffectsRelay
    private var lastState: TopicState = TopicState()

    private val topicState: Observable<TopicState>
        get() = inputRelay.reduxStore(
            initialState = lastState,
            sideEffects = listOf(
                firstLoadMessages(), nextLoadMessages(), loadMessagesByEvent(),
                sendMessage(), updateReaction(), deleteMessage(),
                sendFile(), downloadFile(),
                registerEventQueue(), deleteEventQueue(),
                resetLoadAction(), loadTopics()
            ),
            reducer = TopicState::reduce
        ).doOnNext { lastState = it }

    override fun attachView(view: TopicView) {
        super.attachView(view)

        topicState.observeOn(AndroidSchedulers.mainThread()).startWith(lastState)
            .subscribe(view::render)
            .disposeOnFinish()

        uiEffectsInput.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::handleUiEffect)
            .disposeOnFinish()
    }

    private fun firstLoadMessages(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.FirstLoadMessages::class.java)
                .switchMap { action ->
                    loadMessages(action.streamName, action.topicName, 0)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun nextLoadMessages(): TopicSideEffect {
        return { actions, state ->
            actions.ofType(TopicAction.NextLoadMessages::class.java)
                .switchMap { action ->
                    loadMessages(action.streamName, action.topicName, state().oldestMessageId)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun registerEventQueue(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.RegisterEventQueue::class.java)
                .switchMap { action ->
                    registerEventQueue(action.streamName, action.topicName)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.EventQueueStop
                        }
                }
        }
    }

    private fun deleteEventQueue(): TopicSideEffect {
        return { actions, state ->
            actions.ofType(TopicAction.DeleteEventQueue::class.java)
                .switchMap {
                    deleteEventQueue(state().queueId)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.EventQueueStop
                        }
                }
        }
    }

    private fun loadMessagesByEvent(): TopicSideEffect {
        return { actions, state ->
            actions.ofType(TopicAction.MessagesLoaded::class.java)
                .switchMap {
                    loadMessagesByEvent(state().streamName, state().topicName, state().oldestMessageId, state().queueId)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun resetLoadAction(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.MessagesUpdated::class.java)
                .map { TopicAction.MessagesLoaded(it.newMessages) }
        }
    }

    private fun sendMessage(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.SendMessage::class.java)
                .switchMap { action ->
                    sendMessage(action.streamName, action.topicName, action.content)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun deleteMessage(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.DeleteMessage::class.java)
                .switchMap { action ->
                    deleteMessage(action.messageId)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun updateReaction(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.UpdateReaction::class.java)
                .switchMap { action ->
                    updateReaction(action.message, action.currentUserId, action.reactionCode)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun sendFile(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.SendFile::class.java)
                .switchMap { action ->
                    sendFile(action.streamName, action.topicName, action.name, action.stream)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun downloadFile(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.DownloadFile::class.java)
                .switchMap { action ->
                    downloadFile(action.title, action.url)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun loadTopics(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.LoadTopics::class.java)
                .switchMap { action ->
                    loadTopics(action.streamId, action.streamName)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.LoadTopicsStop
                        }
                }
        }
    }

    private fun registerEventQueue(streamName: String, topicName: String): Observable<TopicAction> {
        return eventRepository.registerTopicEventQueue(streamName, topicName)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { queue -> TopicAction.EventQueueRegistered(queue) }
    }

    private fun deleteEventQueue(queueId: String): Observable<TopicAction> {
        return eventRepository.deleteEventQueue(queueId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { TopicAction.EventQueueStop }
    }

    private fun loadMessages(streamName: String, topicName: String, messageId: Int): Observable<TopicAction> {
        return topicInteractor.loadMessages(streamName, topicName, messageId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> TopicAction.MessagesLoaded(newMessages = items) }
    }

    private fun loadMessagesByEvent(streamName: String, topicName: String, messageId: Int, queueId: String): Observable<TopicAction> {
        return topicInteractor.loadMessagesByEvent(streamName, topicName, messageId, queueId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> TopicAction.MessagesUpdated(newMessages = items) }
    }

    private fun sendMessage(streamName: String, topicName: String, content: String): Observable<TopicAction> {
        return topicInteractor.addMessage(streamName, topicName, content)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> TopicAction.MessagesLoaded(newMessages = items) }
    }

    private fun deleteMessage(messageId: Int): Observable<TopicAction> {
        return topicInteractor.deleteMessage(messageId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { TopicAction.MessageDeleted(messageId = messageId) }
    }

    private fun updateReaction(message: MessageUi, currentUserId: Int, reactionCode: String): Observable<TopicAction> {
        return topicInteractor.updateReaction(message, currentUserId, reactionCode)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> TopicAction.MessagesLoaded(newMessages = items) }
    }

    private fun sendFile(streamName: String, topicName: String, name: String, stream: InputStream?): Observable<TopicAction> {
        return topicInteractor.sendFile(streamName, topicName, name, stream)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> TopicAction.MessagesLoaded(newMessages = items) }
    }

    private fun downloadFile(fileName: String, url: String): Observable<TopicAction> {
        return topicInteractor.downloadFile(url)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { stream ->
                uiEffectsRelay.accept(TopicUiEffect.FileDownloaded(fileName, stream))
                TopicAction.FileDownloaded
            }
    }

    private fun loadTopics(streamId: Int, streamName: String): Observable<TopicAction> {
        return topicRepository.getStreamTopics(streamId, streamName)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { topics ->
                uiEffectsRelay.accept(TopicUiEffect.TopicsLoaded(topics.map { it.name }))
                TopicAction.LoadTopicsStop
            }
    }
}