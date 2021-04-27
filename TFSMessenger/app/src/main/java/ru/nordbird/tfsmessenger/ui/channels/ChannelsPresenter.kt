package ru.nordbird.tfsmessenger.ui.channels

import com.freeletics.rxredux.SideEffect
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.domain.ChannelsInteractor
import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter
import java.util.*
import java.util.concurrent.TimeUnit

private typealias ChannelsSideEffect = SideEffect<ChannelsState, out ChannelsAction>

class ChannelsPresenter(
    private val channelsInteractor: ChannelsInteractor
) : RxPresenter<ChannelsView>(ChannelsView::class.java) {

    val input: Consumer<ChannelsAction> get() = inputRelay

    private val inputRelay: PublishRelay<ChannelsAction> = PublishRelay.create()
    private val uiEffectsRelay: PublishRelay<ChannelsUiEffect> = PublishRelay.create()
    private val uiEffectsInput: Observable<ChannelsUiEffect> get() = uiEffectsRelay
    private var lastState: ChannelsState = ChannelsState()

    private val channelsState: Observable<ChannelsState>
        get() = inputRelay.reduxStore(
            initialState = lastState,
            sideEffects = listOf(loadStreams(), searchStreams(), expandTopics()),
            reducer = ChannelsState::reduce
        ).doOnNext { lastState = it }

    override fun attachView(view: ChannelsView) {
        super.attachView(view)

        channelsState.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::render)
            .disposeOnFinish()

        uiEffectsInput.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::handleUiEffect)
            .disposeOnFinish()
    }

    override fun detachView(isFinishing: Boolean) {
        super.detachView(isFinishing)
        if (isFinishing) {
            lastState = ChannelsState()
        }
    }

    private fun loadStreams(): ChannelsSideEffect {
        return { actions, _ ->
            actions.ofType(ChannelsAction.LoadStreams::class.java)
                .switchMap {
                    getStreams()
                        .onErrorReturn { error -> ChannelsAction.ErrorLoadStreams(error) }
                }
        }
    }

    private fun searchStreams(): ChannelsSideEffect {
        return { actions, state ->
            actions.ofType(ChannelsAction.SearchStreams::class.java)
                .map { action -> action.copy(query = action.query.toLowerCase(Locale.getDefault()).trim()) }
                .distinctUntilChanged()
                .debounce(500, TimeUnit.MILLISECONDS)
                .switchMap {
                    searchStreams(state().filterQuery)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(ChannelsUiEffect.SearchStreamsError(error))
                            ChannelsAction.SearchStreamsStop
                        }
                }
        }
    }

    private fun expandTopics(): ChannelsSideEffect {
        return { actions, _ ->
            actions.ofType(ChannelsAction.ExpandTopics::class.java)
                .switchMap { action ->
                    loadTopics(action.streamId)
                        .onErrorReturn { error -> ChannelsAction.ErrorLoadStreams(error) }
                }
        }
    }

    private fun getStreams(): Observable<ChannelsAction> {
        return channelsInteractor.loadStreams()
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> ChannelsAction.StreamsLoaded(streams = items) }
    }

    private fun searchStreams(query: String): Observable<ChannelsAction> {
        return channelsInteractor.loadStreams(query)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> ChannelsAction.StreamsFound(streams = items) }
    }

    private fun loadTopics(streamId: Int): Observable<ChannelsAction> {
        return channelsInteractor.loadTopics(streamId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> ChannelsAction.TopicsLoaded(items) }
    }

}