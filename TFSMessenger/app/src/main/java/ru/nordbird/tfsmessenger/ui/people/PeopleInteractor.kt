package ru.nordbird.tfsmessenger.ui.people

import ru.nordbird.tfsmessenger.data.mapper.UserToUserUiMapper
import ru.nordbird.tfsmessenger.data.repository.UserRepository
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

object PeopleInteractor {

    private val userRepository = UserRepository
    private val userMapper = UserToUserUiMapper()

    private val users = mutableListOf<UserUi>()

    private var filterQuery = ""

    fun getUsers(): List<UserUi> {
        val userList = userRepository.getUsers().filter {
            it.name.contains(filterQuery, true) || it.email.contains(filterQuery, true)
        }
        users.clear()
        users.addAll(userMapper.transform(userList))
        return users
    }

    fun filterUsers(query: String) {
        filterQuery = query
    }

    fun getUser(userId: String) = users.firstOrNull { it.id == userId }

}