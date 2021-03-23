package ru.nordbird.tfsmessenger.data.repository

import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.data.model.User

object UserRepository {

    private val users = mutableListOf<User>()

    init {
        users.addAll(DataGenerator.getUsers())
    }

    fun getUsers() = users.toList()

}