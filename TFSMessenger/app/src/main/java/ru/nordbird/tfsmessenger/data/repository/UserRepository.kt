package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.dao.AppDatabaseImpl
import ru.nordbird.tfsmessenger.data.mapper.UserDbToUserMapper
import ru.nordbird.tfsmessenger.data.mapper.UserNwToUserDbMapper
import ru.nordbird.tfsmessenger.data.model.PresenceResponse
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.data.model.UserDb

object UserRepository {

    private val nwUserMapper = UserNwToUserDbMapper()
    private val dbUserMapper = UserDbToUserMapper()

    fun getUsers(query: String = ""): Flowable<List<User>> {
        return Single.concat(
            getDatabaseUsers(query),
            getNetworkUsers(query)
                .onErrorResumeNext(getDatabaseUsers(query))
        )
            .map { dbUserMapper.transform(it) }
    }

    fun getUser(id: String): Flowable<User> {
        return Single.concat(
            getDatabaseUser(id),
            getNetworkUser(id)
                .onErrorResumeNext(getDatabaseUser(id))
        )
            .map { dbUserMapper.transform(listOf(it)).firstOrNull() }
    }

    private fun getNetworkUsers(query: String = ""): Single<List<UserDb>> {
        return ZulipServiceImpl.getApi().getUsers()
            .observeOn(Schedulers.computation())
            .flatMapObservable { Observable.fromIterable(it.members) }
            .map { nwUserMapper.transform(it) }
            .flatMap(
                { user ->
                    ZulipServiceImpl.getApi()
                        .getUserPresence(user.id.toString())
                        .onErrorReturnItem(PresenceResponse())
                        .toObservable()
                },
                { user, presence -> addPresence(user, presence) }
            )
            .toList()
            .doOnSuccess { saveToDatabase(it) }
            .map { list -> list.filter { it.full_name.contains(query, true) } }
    }

    private fun getNetworkUser(id: String): Single<UserDb> {
        return Single.zip(
            ZulipServiceImpl.getApi().getUser(id)
                .map { nwUserMapper.transform(it.user) },
            ZulipServiceImpl.getApi().getUserPresence(id)
                .onErrorReturnItem(PresenceResponse()),
            { user, presence -> addPresence(user, presence) }
        )
    }

    private fun getDatabaseUsers(query: String = ""): Single<List<UserDb>> {
        return AppDatabaseImpl.userDao().getAll(query)
    }

    private fun getDatabaseUser(id: String): Single<UserDb> {
        return AppDatabaseImpl.userDao().getById(id.toIntOrNull() ?: 0)
    }

    private fun addPresence(user: UserDb, presenceResponse: PresenceResponse): UserDb {
        user.timestamp = presenceResponse.presence.maxOfOrNull { it.value.timestamp } ?: 0
        return user
    }

    private fun saveToDatabase(users: List<UserDb>) {
        AppDatabaseImpl.userDao().insertAll(users)
    }

}