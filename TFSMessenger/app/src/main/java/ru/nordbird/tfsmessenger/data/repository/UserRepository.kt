package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.UserDao
import ru.nordbird.tfsmessenger.data.mapper.UserDbToUserMapper
import ru.nordbird.tfsmessenger.data.mapper.UserNwToUserDbMapper
import ru.nordbird.tfsmessenger.data.model.PresenceResponse
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.data.model.UserDb

class UserRepository(
    private val apiService: ZulipService,
    private val userDao: UserDao
) {

    private val nwUserMapper = UserNwToUserDbMapper()
    private val dbUserMapper = UserDbToUserMapper()

    fun getUsers(query: String = ""): Flowable<List<User>> {
        return Single.concat(
            getDatabaseUsers(query),
            getNetworkUsers(query)
        )
            .map { dbUserMapper.transform(it) }
    }

    fun getUser(id: Int): Flowable<User> {
        return Single.concat(
            getDatabaseUser(id),
            getNetworkUser(id)
        )
            .map { dbUserMapper.transform(listOf(it)).first() }
    }

    private fun getNetworkUsers(query: String = ""): Single<List<UserDb>> {
        return apiService.getUsers()
            .flatMapObservable { response ->
                Observable.fromIterable(response.members
                    .map { nwUserMapper.transform(it) })
            }
            .flatMap(
                { user ->
                    apiService.getUserPresence(user.id)
                        .onErrorReturnItem(PresenceResponse())
                        .toObservable()
                },
                { user, presence -> addPresence(user, presence) }
            )
            .toList()
            .doOnSuccess { saveToDatabase(it) }
            .map { list -> list.filter { it.full_name.contains(query, true) } }
    }

    private fun getNetworkUser(id: Int): Single<UserDb> {
        return Single.zip(
            apiService.getUser(id)
                .map { nwUserMapper.transform(it.user) },
            apiService.getUserPresence(id)
                .onErrorReturnItem(PresenceResponse()),
            { user, presence -> addPresence(user, presence) }
        )
    }

    private fun getDatabaseUsers(query: String = ""): Single<List<UserDb>> {
        return userDao.getUsers(query)
    }

    private fun getDatabaseUser(id: Int): Single<UserDb> {
        return userDao.getById(id)
    }

    private fun addPresence(user: UserDb, presenceResponse: PresenceResponse): UserDb {
        user.timestamp = presenceResponse.presence.maxOfOrNull { it.value.timestamp } ?: 0
        return user
    }

    private fun saveToDatabase(users: List<UserDb>) {
        userDao.insertAll(users)
    }

}