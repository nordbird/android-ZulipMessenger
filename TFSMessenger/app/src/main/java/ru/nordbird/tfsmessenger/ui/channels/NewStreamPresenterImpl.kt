package ru.nordbird.tfsmessenger.ui.channels

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

class NewStreamPresenterImpl(
    private val channelsInteractor: ChannelsInteractor
) : ChannelsPresenter() {

    override val input: Consumer<ChannelsAction> get() = inputRelay

    private val inputRelay: PublishRelay<ChannelsAction> = PublishRelay.create()
    private val uiEffectsRelay: PublishRelay<ChannelsUiEffect> = PublishRelay.create()
    private val uiEffectsInput: Observable<ChannelsUiEffect> get() = uiEffectsRelay

    private val channelsState: Observable<ChannelsState>
        get() = inputRelay.reduxStore(
            initialState = ChannelsState(),
            sideEffects = listOf(loadStreams(), createStream()),
            reducer = ChannelsState::reduce
        )

    override fun attachView(view: ChannelsView) {
        super.attachView(view)

        channelsState.observeOn(AndroidSchedulers.mainThread())
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

    private fun createStream(): ChannelsSideEffect {
        return { actions, _ ->
            actions.ofType(ChannelsAction.CreateStream::class.java)
                .switchMap {
                    createStream(it.streamName)
                        .doOnNext { uiEffectsRelay.accept(ChannelsUiEffect.StreamCreated) }
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(ChannelsUiEffect.ActionError(error))
                            ChannelsAction.LoadStreamsStop
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

    private fun createStream(streamName: String): Observable<ChannelsAction> {
        return channelsInteractor.createStream(streamName)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { ChannelsAction.StreamCreated }
    }
}