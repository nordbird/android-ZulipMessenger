package ru.nordbird.tfsmessenger.domain

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.mapper.UserToUserUiMapper
import ru.nordbird.tfsmessenger.data.model.Presence
import ru.nordbird.tfsmessenger.data.repository.base.UserRepository
import ru.nordbird.tfsmessenger.domain.base.PeopleInteractor
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

class PeopleInteractorImpl(
    private val userRepository: UserRepository
) : PeopleInteractor {

    private val userMapper = UserToUserUiMapper()

    override fun loadUsers(): Flowable<List<UserUi>> {
        return userRepository.getUsers()
            .map { users -> userMapper.transform(users) }
    }

    override fun loadUser(userId: Int): Flowable<UserUi> {
        return userRepository.getUser(userId)
            .map { user -> userMapper.transform(listOf(user)).first() }
    }

    override fun loadUserPresence(userId: Int): Single<Presence> {
        return userRepository.getUserPresence(userId)
    }

}