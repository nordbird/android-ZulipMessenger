package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.*

class UserNwToUserDbMapper : Mapper<List<UserNw>, List<UserDb>> {

    override fun transform(data: List<UserNw>): List<UserDb> {
        return data.map { UserDb(it.id, it.full_name, it.email, it.avatar_url) }
    }
}