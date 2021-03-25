package ru.nordbird.tfsmessenger.data.repository

import ru.nordbird.tfsmessenger.data.DataGenerator

object UserRepository {

    private val users = DataGenerator.getUsers()

    fun getUsers() = users

}