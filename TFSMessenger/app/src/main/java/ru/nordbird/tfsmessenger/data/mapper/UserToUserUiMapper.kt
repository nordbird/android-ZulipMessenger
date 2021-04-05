package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.*
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserPresence
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

class UserToUserUiMapper : Mapper<List<User>, List<UserUi>> {

    override fun transform(data: List<User>): List<UserUi> {
        return data.map { UserUi(it.id.toString(), it.full_name, it.email, it.avatar_url, convertPresence(it.presence)) }
    }

    private fun convertPresence(presence: Presence): UserPresence {
        return when (presence.status) {
            PRESENCE_STATUS_ACTIVE -> UserPresence.ACTIVE
            PRESENCE_STATUS_IDLE -> UserPresence.IDLE
            else -> UserPresence.OFFLINE
        }
    }
}