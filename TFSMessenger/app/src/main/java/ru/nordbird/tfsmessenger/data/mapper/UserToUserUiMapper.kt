package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserPresence
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

class UserToUserUiMapper : Mapper<List<User>, List<UserUi>> {

    override fun transform(data: List<User>): List<UserUi> {
        return data.map { UserUi(it.id, it.full_name, it.email, it.avatar_url, 0) }
    }

}