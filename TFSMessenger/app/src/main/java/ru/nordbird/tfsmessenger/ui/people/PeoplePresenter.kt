package ru.nordbird.tfsmessenger.ui.people

import com.freeletics.rxredux.SideEffect
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.domain.PeopleInteractor
import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter
import java.util.*
import java.util.concurrent.TimeUnit

private typealias PeopleSideEffect = SideEffect<PeopleState, out PeopleAction>

class PeoplePresenter(
    private val peopleInteractor: PeopleInteractor
) : RxPresenter<PeopleView>(PeopleView::class.java) {

    val input: Consumer<PeopleAction> get() = inputRelay

    private val inputRelay: PublishRelay<PeopleAction> = PublishRelay.create()
    private val uiEffectsRelay: PublishRelay<PeopleUiEffect> = PublishRelay.create()
    private val uiEffectsInput: Observable<PeopleUiEffect> get() = uiEffectsRelay
    private var lastState: PeopleState = PeopleState()

    private val peopleState: Observable<PeopleState>
        get() = inputRelay.reduxStore(
            initialState = lastState,
            sideEffects = listOf(loadUsers(), searchUsers()),
            reducer = PeopleState::reduce
        ).doOnNext { lastState = it }

    override fun attachView(view: PeopleView) {
        super.attachView(view)

        peopleState.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::render)
            .disposeOnFinish()

        uiEffectsInput.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::handleUiEffect)
            .disposeOnFinish()
    }

    override fun detachView(isFinishing: Boolean) {
        super.detachView(isFinishing)
        if (isFinishing) {
            lastState = PeopleState()
        }
    }

    private fun loadUsers(): PeopleSideEffect {
        return { actions, state ->
            actions.ofType(PeopleAction.LoadUsers::class.java)
                .switchMap {
                    searchUsers(state().filterQuery)
                        .onErrorReturn { error -> PeopleAction.ErrorLoadUsers(error) }
                }
        }
    }

    private fun searchUsers(): PeopleSideEffect {
        return { actions, state ->
            actions.ofType(PeopleAction.SearchUsers::class.java)
                .map { action -> action.copy(query = action.query.toLowerCase(Locale.getDefault()).trim()) }
                .distinctUntilChanged()
                .debounce(500, TimeUnit.MILLISECONDS)
                .switchMap {
                    searchUsers(state().filterQuery)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(PeopleUiEffect.SearchUsersError(error))
                            PeopleAction.SearchUsersStop
                        }
                }
        }
    }

    private fun searchUsers(query: String = ""): Observable<PeopleAction> {
        return peopleInteractor.loadUsers(query)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> PeopleAction.UsersLoaded(items = items) }
    }

}