package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.UserDao
import ru.nordbird.tfsmessenger.data.mapper.UserDbToUserMapper
import ru.nordbird.tfsmessenger.data.mapper.UserNwToUserDbMapper
import ru.nordbird.tfsmessenger.data.model.Presence
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.data.model.UserDb

class UserRepository(
    private val apiService: ZulipService,
    private val userDao: UserDao
) {

    private val nwUserMapper = UserNwToUserDbMapper()
    private val dbUserMapper = UserDbToUserMapper()

    fun getUsers(): Flowable<List<User>> {
        return Single.concat(
            getDatabaseUsers(),
            getNetworkUsers()
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

    fun getUserPresence(userId: Int): Single<Presence> {
        return apiService.getUserPresence(userId)
            .map { response ->
                Presence(userId, response.presence.maxOfOrNull { it.value.timestamp_sec } ?: 0)
            }
    }

    private fun getNetworkUsers(): Single<List<UserDb>> {
        return apiService.getUsers()
            .map { response -> nwUserMapper.transform(response.members) }
            .doOnSuccess { saveToDatabase(it) }
    }

    private fun getNetworkUser(id: Int): Single<UserDb> {
        return apiService.getUser(id)
            .map { response -> nwUserMapper.transform(listOf(response.user)).first() }
    }

    private fun getDatabaseUsers(): Single<List<UserDb>> {
        return userDao.getUsers()
    }

    private fun getDatabaseUser(id: Int): Single<UserDb> {
        return userDao.getById(id)
    }

    private fun saveToDatabase(users: List<UserDb>) {
        userDao.insertAll(users)
    }

}