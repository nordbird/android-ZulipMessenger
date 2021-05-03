package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.*

class UserDbToUserMapper : Mapper<List<UserDb>, List<User>> {

    override fun transform(data: List<UserDb>): List<User> {
        return data.map { User(it.id, it.full_name, it.email, it.avatar_url) }
    }
}