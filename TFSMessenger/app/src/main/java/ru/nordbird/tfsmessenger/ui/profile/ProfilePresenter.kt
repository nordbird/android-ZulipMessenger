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
import ru.nordbird.tfsmessenger.domain.PeopleInteractor

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
        sideEffects = listOf(loadUser()),
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
        return { actions, state ->
            actions.ofType(ProfileAction.LoadProfile::class.java)
                .switchMap {
                    getUser(state().userId)
                        .onErrorReturn { error -> ProfileAction.ErrorLoadProfile(error) }
                }
        }
    }

    private fun getUser(userId: Int): Observable<ProfileAction> {
        return peopleInteractor.getUser(userId)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .map { item -> ProfileAction.ProfileLoaded(item = item) }
    }

}