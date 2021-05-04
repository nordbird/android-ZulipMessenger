package ru.nordbird.tfsmessenger.domain.base

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.Presence
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

interface PeopleInteractor {

    fun loadUsers(): Flowable<List<UserUi>>

    fun loadUser(userId: Int): Flowable<UserUi>

    fun loadUserPresence(userId: Int): Single<Presence>

}