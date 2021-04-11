package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.*

class UserNwToUserDbMapper : Mapper<UserNw, UserDb> {

    override fun transform(data: UserNw): UserDb {
        return UserDb(data.id, data.full_name, data.email, data.avatar_url, data.timestamp)
    }
}