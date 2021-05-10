package ru.nordbird.tfsmessenger.ui.topic

import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.data.repository.base.TopicRepository
import ru.nordbird.tfsmessenger.domain.base.TopicInteractor
import ru.nordbird.tfsmessenger.ui.topic.base.TopicAction
import ru.nordbird.tfsmessenger.ui.topic.base.TopicPresenter
import ru.nordbird.tfsmessenger.ui.topic.base.TopicUiEffect
import ru.nordbird.tfsmessenger.ui.topic.base.TopicView

class EditMessagePresenterImpl(
    private val topicInteractor: TopicInteractor,
    private val topicRepository: TopicRepository
) : TopicPresenter() {

    override val input: Consumer<TopicAction> get() = inputRelay

    private val inputRelay: PublishRelay<TopicAction> = PublishRelay.create()
    private val uiEffectsRelay: PublishRelay<TopicUiEffect> = PublishRelay.create()
    private val uiEffectsInput: Observable<TopicUiEffect> get() = uiEffectsRelay

    private val topicState: Observable<TopicState>
        get() = inputRelay.reduxStore(
            initialState = TopicState(),
            sideEffects = listOf(loadMessageContent(), loadTopics(), updateMessage()),
            reducer = TopicState::reduce
        )

    override fun attachView(view: TopicView) {
        super.attachView(view)

        topicState.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::render)
            .disposeOnFinish()

        uiEffectsInput.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::handleUiEffect)
            .disposeOnFinish()
    }

    private fun loadMessageContent(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.LoadMessage::class.java)
                .switchMap { action ->
                    loadMessageContent(action.messageId)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(TopicUiEffect.ActionError(error))
                            TopicAction.LoadMessagesStop
                        }
                }
        }
    }

    private fun updateMessage(): TopicSideEffect {
        return { actions, _ ->
            actions.ofType(TopicAction.UpdateMessage::class.java)
                .switchMap { action ->
                    updateMessage(action.messageId, action.topicName, action.content)
                        .doOnNext { uiEffectsRelay.accept(TopicUiEffect.MessageUpdated) }
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

    private fun loadMessageContent(messageId: Int): Observable<TopicAction> {
        return topicInteractor.loadMessageContent(messageId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { content ->
                uiEffectsRelay.accept(TopicUiEffect.MessageLoaded(content))
                TopicAction.LoadMessagesStop
            }
    }

    private fun updateMessage(messageId: Int, topicName: String, content: String): Observable<TopicAction> {
        return topicInteractor.updateMessage(messageId, topicName, content)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { TopicAction.MessageUpdated }
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