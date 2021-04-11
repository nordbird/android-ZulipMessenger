package ru.nordbird.tfsmessenger.ui.people

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.data.mapper.UserToUserUiMapper
import ru.nordbird.tfsmessenger.data.repository.UserRepository
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi
import java.util.*
import java.util.concurrent.TimeUnit

object PeopleInteractor {

    private val userRepository = UserRepository
    private val userMapper = UserToUserUiMapper()

    fun getUsers(query: String = ""): Flowable<List<UserUi>> = userRepository.getUsers(query)
        .map { users -> userMapper.transform(users) }
        .map { users -> users.sortedBy { it.name } }
        .subscribeOn(Schedulers.io())

    fun filterUsers(searchObservable: Observable<String>): Observable<List<UserUi>> {
        return searchObservable
            .map { query -> query.toLowerCase(Locale.getDefault()).trim() }
            .distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
            .switchMap { query -> getUsers(query).toObservable() }
            .onErrorReturnItem(emptyList())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUser(userId: String): Flowable<UserUi?> =
        userRepository.getUser(userId)
            .map { user -> userMapper.transform(listOf(user)).firstOrNull() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

}