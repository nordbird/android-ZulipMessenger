package ru.nordbird.tfsmessenger.data.repository.base

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.Presence
import ru.nordbird.tfsmessenger.data.model.User

interface UserRepository {

    fun getUsers(): Flowable<List<User>>

    fun getUser(id: Int): Flowable<User>

    fun getUserPresence(userId: Int): Single<Presence>

}