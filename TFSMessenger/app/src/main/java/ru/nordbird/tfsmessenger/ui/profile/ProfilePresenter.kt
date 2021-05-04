package ru.nordbird.tfsmessenger.ui.profile

import com.freeletics.rxredux.SideEffect
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.RxPresenter
import ru.nordbird.tfsmessenger.domain.base.PeopleInteractor
import java.util.concurrent.TimeUnit

private typealias PeopleSideEffect = SideEffect<ProfileState, out ProfileAction>

class ProfilePresenter(
    private val peopleInteractor: PeopleInteractor
) : RxPresenter<ProfileView>(ProfileView::class.java) {

    private val inputRelay: Relay<ProfileAction> = PublishRelay.create()
    private val uiEffectsRelay = PublishRelay.create<ProfileUiEffect>()

    val input: Consumer<ProfileAction> get() = inputRelay
    private val uiEffectsInput: Observable<ProfileUiEffect> get() = uiEffectsRelay

    private val peopleState: Observable<ProfileState> = inputRelay.reduxStore(
        initialState = ProfileState(),
        sideEffects = listOf(loadUser(), loadUserPresence()),
        reducer = ProfileState::reduce
    )

    override fun attachView(view: ProfileView) {
        super.attachView(view)
        peopleState.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::render)
            .disposeOnFinish()
        uiEffectsInput.observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::handleUiEffect)
            .disposeOnFinish()
    }

    private fun loadUser(): PeopleSideEffect {
        return { actions, _ ->
            actions.ofType(ProfileAction.LoadProfile::class.java)
                .switchMap {
                    getUser(it.userId)
                        .onErrorReturn { error ->
                            uiEffectsRelay.accept(ProfileUiEffect.LoadUserError(error))
                            ProfileAction.LoadProfileStop
                        }
                }
        }
    }

    private fun loadUserPresence(): PeopleSideEffect {
        return { actions, _ ->
            actions.ofType(ProfileAction.ProfileLoaded::class.java)
                .distinctUntilChanged()
                .debounce(500, TimeUnit.MILLISECONDS)
                .switchMap { action ->
                    getUserPresence(action.item.id)
                        .onErrorReturnItem(ProfileAction.LoadPresenceStop)
                }
        }
    }

    private fun getUser(userId: Int): Observable<ProfileAction> {
        return peopleInteractor.loadUser(userId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { item -> ProfileAction.ProfileLoaded(item = item) }
    }

    private fun getUserPresence(userId: Int): Observable<ProfileAction> {
        return peopleInteractor.loadUserPresence(userId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { presence -> ProfileAction.PresenceLoaded(presence) }
    }
}