package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Observable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.model.DEFAULT_PRESENCE_KEY
import ru.nordbird.tfsmessenger.data.model.PresenceResponse
import ru.nordbird.tfsmessenger.data.model.User
import java.util.*

object UserRepository {

    fun getUsers(query: String = ""): Single<List<User>> = ZulipServiceImpl.getApi().getUsers()
        .flatMapObservable { Observable.fromIterable(it.members) }
        .filter{ it.full_name.toLowerCase(Locale.getDefault()).contains(query) }
        .flatMap(
            { user ->
                ZulipServiceImpl.getApi()
                    .getUserPresence(user.id.toString())
                    .onErrorReturnItem(PresenceResponse())
                    .toObservable()
            },
            { user, presence -> addPresence(user, presence) }
        )
        .toList()

    fun getUser(id: String): Single<User> = Single
        .zip(
            ZulipServiceImpl.getApi().getUser(id),
            ZulipServiceImpl.getApi()
                .getUserPresence(id)
                .onErrorReturnItem(PresenceResponse()),
            { response, presence -> addPresence(response.user, presence) }
        )

    private fun addPresence(user: User, presenceResponse: PresenceResponse): User {
        if (presenceResponse.presence.containsKey(DEFAULT_PRESENCE_KEY)) {
            user.presence = presenceResponse.presence[DEFAULT_PRESENCE_KEY]!!
        }

        return user
    }

}