package ru.nordbird.tfsmessenger.domain

import io.reactivex.Flowable
import ru.nordbird.tfsmessenger.data.mapper.UserToUserUiMapper
import ru.nordbird.tfsmessenger.data.repository.UserRepository
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

class PeopleInteractor(
    private val userRepository: UserRepository
) {

    private val userMapper = UserToUserUiMapper()

    fun getUsers(query: String = ""): Flowable<List<UserUi>> {
        return userRepository.getUsers(query)
            .map { users -> userMapper.transform(users).sortedBy { it.name } }
    }

    fun getUser(userId: Int): Flowable<UserUi> {
        return userRepository.getUser(userId)
            .map { user -> userMapper.transform(listOf(user)).first() }
    }

}