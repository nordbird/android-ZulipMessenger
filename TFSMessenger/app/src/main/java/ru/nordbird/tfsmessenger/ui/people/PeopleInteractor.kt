package ru.nordbird.tfsmessenger.ui.people

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.SingleSubject
import ru.nordbird.tfsmessenger.data.mapper.UserToUserUiMapper
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.data.repository.UserRepository
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi
import java.util.*
import java.util.concurrent.TimeUnit

object PeopleInteractor {

    private val userRepository = UserRepository
    private val userMapper = UserToUserUiMapper()

    private val users: SingleSubject<List<UserUi>> = SingleSubject.create()

    fun getUsers(): Single<List<UserUi>> = userRepository.getUsers()
        .map { users -> transformUsers(users) }
        .map { users -> users.sortedBy { it.name } }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(users)

    fun filterUsers(searchObservable: Observable<String>): Observable<List<UserUi>> {
        return searchObservable
            .map { query -> query.toLowerCase(Locale.getDefault()).trim() }
            .distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
            .flatMap { query ->
                users.map { list ->
                    list.filter { it.name.toLowerCase(Locale.getDefault()).contains(query) }
                }.toObservable()
            }
            .onErrorReturnItem(emptyList())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUser(userId: String): Single<UserUi?> =
        users.map { list -> list.firstOrNull { it.id == userId } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun transformUsers(users: List<User>): List<UserUi> {
        return userMapper.transform(users)
    }
}