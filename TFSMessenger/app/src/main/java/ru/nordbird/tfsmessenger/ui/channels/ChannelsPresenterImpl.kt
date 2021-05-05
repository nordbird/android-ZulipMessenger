package ru.nordbird.tfsmessenger.ui.channels

import com.freeletics.rxredux.SideEffect
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.domain.base.ChannelsInteractor
import ru.nordbird.tfsmessenger.ui.channels.base.ChannelsAction
import ru.nordbird.tfsmessenger.ui.channels.base.ChannelsPresenter
import ru.nordbird.tfsmessenger.ui.channels.base.ChannelsUiEffect
import ru.nordbird.tfsmessenger.ui.channels.base.ChannelsView
import java.util.*
import java.util.concurrent.TimeUnit

private typealias ChannelsSideEffect = SideEffect<ChannelsState, out ChannelsAction>

class ChannelsPresenterImpl(
    private val channelsInteractor: ChannelsInteractor
) : ChannelsPresenter() {

    override val input: Consumer<ChannelsAction> get() = inputRelay

    private val inputRelay: PublishRelay<ChannelsAction> = PublishRelay.create()
    private val uiEffectsRelay: PublishRelay<ChannelsUiEffect> = PublishRelay.create()
    private val uiEffectsInput: Observable<ChannelsUiEffect> get() = uiEffectsRelay
    private var lastState: ChannelsState = ChannelsState()

    private val channelsState: Observable<ChannelsState>
        get() = inputRelay.reduxStore(
            initialState = lastState,
            sideEffects = listOf(loadStreams(), filterStreams(), expandTopics(), loadTopicUnreadMessages()),
            reducer = ChannelsState::reduce
        ).doOnNext { lastState = it }

    override fun attachView(view: ChannelsView) {
        super.attachView(view)

        channelsState.observeOn(AndroidSchedulers.mainThread()).startWith(lastState)
            .subscribe(view::render)
            .disposeOnFinish()

        uiEffectsInput.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::handleUiEffect)
            .disposeOnFinish()
    }

    private fun loadStreams(): ChannelsSideEffect {
        return { actions, _ ->
            actions.ofType(ChannelsAction.LoadStreams::class.java)
                .switchMap {
                    getStreams()
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(ChannelsUiEffect.ActionError(error))
                            ChannelsAction.LoadStreamsStop
                        }
                }
        }
    }

    private fun filterStreams(): ChannelsSideEffect {
        return { actions, _ ->
            actions.ofType(ChannelsAction.FilterStreams::class.java)
                .map { action -> action.query.toLowerCase(Locale.getDefault()).trim() }
                .distinctUntilChanged()
                .debounce(500, TimeUnit.MILLISECONDS)
                .switchMap { Observable.just(ChannelsAction.StreamsFiltered) }
        }
    }

    private fun expandTopics(): ChannelsSideEffect {
        return { actions, _ ->
            actions.ofType(ChannelsAction.ExpandTopics::class.java)
                .switchMap { action ->
                    loadTopics(action.streamId, action.streamName)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(ChannelsUiEffect.ActionError(error))
                            ChannelsAction.FilterStreamsStop
                        }
                }
        }
    }

    private fun loadTopicUnreadMessages(): ChannelsSideEffect {
        return { actions, _ ->
            actions.ofType(ChannelsAction.TopicsLoaded::class.java)
                .distinctUntilChanged()
                .debounce(1000, TimeUnit.MILLISECONDS)
                .flatMapIterable { it.topics }
                .flatMap { topic ->
                    getTopicUnreadMessageCount(topic.streamName, topic.name)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(ChannelsUiEffect.ActionError(error))
                            ChannelsAction.LoadTopicUnreadMessagesStop
                        }
                }
        }
    }

    private fun getStreams(): Observable<ChannelsAction> {
        return channelsInteractor.loadStreams()
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> ChannelsAction.StreamsLoaded(streams = items) }
    }

    private fun loadTopics(streamId: Int, streamName: String): Observable<ChannelsAction> {
        return channelsInteractor.loadTopics(streamId, streamName)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> ChannelsAction.TopicsLoaded(items) }
    }

    private fun getTopicUnreadMessageCount(streamName: String, topicName: String): Observable<ChannelsAction> {
        return channelsInteractor.getTopicUnreadMessageCount(streamName, topicName)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { unreadMessageCounter -> ChannelsAction.TopicUnreadMessagesLoaded(unreadMessageCounter) }
    }
}