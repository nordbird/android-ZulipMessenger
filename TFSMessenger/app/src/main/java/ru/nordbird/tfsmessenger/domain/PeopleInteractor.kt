package ru.nordbird.tfsmessenger.domain

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.mapper.UserToUserUiMapper
import ru.nordbird.tfsmessenger.data.model.Presence
import ru.nordbird.tfsmessenger.data.repository.UserRepository
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

class PeopleInteractor(
    private val userRepository: UserRepository
) {

    private val userMapper = UserToUserUiMapper()

    fun loadUsers(): Flowable<List<UserUi>> {
        return userRepository.getUsers()
            .map { users -> userMapper.transform(users) }
    }

    fun loadUser(userId: Int): Flowable<UserUi> {
        return userRepository.getUser(userId)
            .map { user -> userMapper.transform(listOf(user)).first() }
    }

    fun loadUserPresence(userId: Int): Single<Presence> {
        return userRepository.getUserPresence(userId)
    }

}