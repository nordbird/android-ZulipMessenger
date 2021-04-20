package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.*
import ru.nordbird.tfsmessenger.extensions.SECOND
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserPresence
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi
import java.util.*

class UserToUserUiMapper : Mapper<List<User>, List<UserUi>> {

    override fun transform(data: List<User>): List<UserUi> {
        return data.map { UserUi(it.id, it.full_name, it.email, it.avatar_url, convertPresence(it.timestamp)) }
    }

    private fun convertPresence(timestamp: Int): UserPresence {
        val now = Date().time / SECOND
        return when (now - timestamp) {
            in 0..ACTIVE_MAX_TIME_SEC -> UserPresence.ACTIVE
            in ACTIVE_MAX_TIME_SEC + 1..IDLE_MAX_TIME_SEC -> UserPresence.IDLE
            else -> UserPresence.OFFLINE
        }
    }

    companion object {
        const val ACTIVE_MAX_TIME_SEC = 600
        const val IDLE_MAX_TIME_SEC = 1800
    }
}