package ru.nordbird.tfsmessenger.ui.people

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ru.nordbird.tfsmessenger.data.mapper.UserToUserUiMapper
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.data.repository.UserRepository
import ru.nordbird.tfsmessenger.ui.recycler.holder.ErrorUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi
import java.util.*
import java.util.concurrent.TimeUnit

object PeopleInteractor {

    private val userRepository = UserRepository
    private val userMapper = UserToUserUiMapper()

    private val users: BehaviorSubject<List<UserUi>> = BehaviorSubject.create()
    private val filterQuery: BehaviorSubject<String> = BehaviorSubject.create()
    private val compositeDisposable = CompositeDisposable()

    init {
        filterQuery.onNext("")
//        compositeDisposable.add(loadUsers())
        loadUsers()
    }

    fun clearDisposable() {
        compositeDisposable.clear()
    }

    fun getUsers() = BehaviorSubject.combineLatest(users, filterQuery) { resource, query ->
        resource.filter { it.name.contains(query, true) }
    }

    fun loadUsers() {
        userRepository.getUsers()
            .flatMap { resource -> transformUsers(resource) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (users)
    }

    fun filterUsers(searchObservable: Observable<String>): Disposable {
        return searchObservable
            .map { query -> query.toLowerCase(Locale.getDefault()).trim() }
            .distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .switchMap { query -> Observable.just(query) }
            .subscribe { filterQuery.onNext(it) }
    }

    fun getUser(userId: String): Observable<UserUi> = users.map { list -> list.firstOrNull { it.id == userId } }

    private fun transformUsers(resource: List<User>): Observable<List<UserUi>> {
        return userMapper.transform(resource)
    }
}