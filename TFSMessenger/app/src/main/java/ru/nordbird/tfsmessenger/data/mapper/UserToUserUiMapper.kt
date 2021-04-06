package ru.nordbird.tfsmessenger.data.mapper

import io.reactivex.Observable
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

class UserToUserUiMapper : Mapper<List<User>, Observable<List<UserUi>>> {

    override fun transform(data: List<User>): Observable<List<UserUi>> {
        return Observable.fromArray(data.map { UserUi(it.id, it.name, it.email, it.isOnline) })
    }

}