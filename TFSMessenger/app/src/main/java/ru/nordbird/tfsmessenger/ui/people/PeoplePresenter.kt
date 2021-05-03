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
            sideEffects = listOf(loadUsers(), filterUsers(), loadUserPresence()),
            reducer = PeopleState::reduce
        ).doOnNext { lastState = it }

    override fun attachView(view: PeopleView) {
        super.attachView(view)

        peopleState.observeOn(AndroidSchedulers.mainThread()).startWith(lastState)
            .subscribe(view::render)
            .disposeOnFinish()

        uiEffectsInput.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::handleUiEffect)
            .disposeOnFinish()
    }

    private fun loadUsers(): PeopleSideEffect {
        return { actions, _ ->
            actions.ofType(PeopleAction.LoadUsers::class.java)
                .switchMap {
                    getUsers()
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(PeopleUiEffect.ActionError(error))
                            PeopleAction.LoadUsersStop
                        }
                }
        }
    }

    private fun filterUsers(): PeopleSideEffect {
        return { actions, _ ->
            actions.ofType(PeopleAction.SearchUsers::class.java)
                .map { action -> action.query.toLowerCase(Locale.getDefault()).trim() }
                .distinctUntilChanged()
                .debounce(500, TimeUnit.MILLISECONDS)
                .switchMap { Observable.just(PeopleAction.UsersFiltered) }
        }
    }

    private fun loadUserPresence(): PeopleSideEffect {
        return { actions, _ ->
            actions.ofType(PeopleAction.UsersLoaded::class.java)
                .distinctUntilChanged()
                .debounce(1000, TimeUnit.MILLISECONDS)
                .flatMapIterable { it.users }
                .flatMap { user ->
                    getUserPresence(user.id)
                        .onErrorReturnItem(PeopleAction.LoadUserPresenceStop)
                }
        }
    }

    private fun getUsers(): Observable<PeopleAction> {
        return peopleInteractor.loadUsers()
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { items -> PeopleAction.UsersLoaded(users = items) }
    }

    private fun getUserPresence(userId: Int): Observable<PeopleAction> {
        return peopleInteractor.loadUserPresence(userId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { presence -> PeopleAction.UserPresenceLoaded(presence) }
    }
}