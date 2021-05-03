package ru.nordbird.tfsmessenger.ui.topic

import com.freeletics.rxredux.SideEffect
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.domain.TopicInteractor
import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import java.io.InputStream

private typealias TopicSideEffect = SideEffect<TopicState, out TopicAction>

class TopicPresenter(
    private val topicInteractor: TopicInteractor
) : RxPresenter<TopicView>(TopicView::class.java) {

    val input: Consumer<TopicAction> get() = inputRelay

    private val inputRelay: PublishRelay<TopicAction> = PublishRelay.create()
    private val uiEffectsRelay: PublishRelay<TopicUiEffect> = PublishRelay.create()
    private val uiEffectsInput: Observable<TopicUiEffect> get() = uiEffectsRelay
    private var lastState: TopicState = TopicState()

    private val topicState: Observable<TopicState>
        get() = inputRelay.reduxStore(
            initialState = lastState,
            sideEffects = listOf(firstLoadMessages(), nextLoadMessages(), sendMessage(), updateReaction(), sendFile(), downloadFile()),
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
                            uiEffectsRelay.accept(TopicUiEffect.LoadMessagesError(error))
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
                            uiEffectsRelay.accept(TopicUiEffect.LoadMessagesError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun sendMessage(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.SendMessage::class.java)
                .switchMap { action ->
                    sendMessage(action.streamName, action.topicName, action.content)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.LoadMessagesError(error))
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
                            uiEffectsRelay.accept(TopicUiEffect.LoadMessagesError(error))
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
                            uiEffectsRelay.accept(TopicUiEffect.LoadMessagesError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun downloadFile(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.DownloadFile::class.java)
                .switchMap { action ->
                    downloadFile(action.url)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.LoadMessagesError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun loadMessages(streamName: String, topicName: String, messageId: Int): Observable<TopicAction> {
        return topicInteractor.loadMessages(streamName, topicName, messageId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> TopicAction.MessagesLoaded(newMessages = items) }
    }

    private fun sendMessage(streamName: String, topicName: String, content: String): Observable<TopicAction> {
        return topicInteractor.addMessage(streamName, topicName, content)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> TopicAction.MessagesLoaded(newMessages = items) }
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

    private fun downloadFile(url: String): Observable<TopicAction> {
        return topicInteractor.downloadFile(url)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { stream -> TopicAction.FileDownloaded(stream = stream) }
    }

}