package ru.nordbird.tfsmessenger.di

import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.dao.AppDatabaseImpl
import ru.nordbird.tfsmessenger.data.repository.StreamRepository
import ru.nordbird.tfsmessenger.data.repository.UserRepository
import ru.nordbird.tfsmessenger.domain.ChannelsInteractor
import ru.nordbird.tfsmessenger.domain.PeopleInteractor
import ru.nordbird.tfsmessenger.ui.channels.ChannelsPresenter
import ru.nordbird.tfsmessenger.ui.channels.ChannelsTabType
import ru.nordbird.tfsmessenger.ui.people.PeoplePresenter
import ru.nordbird.tfsmessenger.ui.profile.ProfilePresenter

class GlobalDI private constructor() {

    private val peopleRepository by lazy { UserRepository(ZulipServiceImpl.getApi(), AppDatabaseImpl.getApi()) }
    private val peopleInteractor by lazy { PeopleInteractor(peopleRepository) }
    val peoplePresenter by lazy { PeoplePresenter(peopleInteractor) }
    val profilePresenter by lazy { ProfilePresenter(peopleInteractor) }

    private val channelsRepository by lazy { StreamRepository(ZulipServiceImpl.getApi(), AppDatabaseImpl.getApi()) }
    private val subscriptionInteractor by lazy { ChannelsInteractor(ChannelsTabType.SUBSCRIBED, channelsRepository) }
    private val streamInteractor by lazy { ChannelsInteractor(ChannelsTabType.ALL, channelsRepository) }
    val subscriptionPresenter by lazy { ChannelsPresenter(subscriptionInteractor) }
    val streamPresenter by lazy { ChannelsPresenter(streamInteractor) }


    companion object {

        lateinit var INSTANCE: GlobalDI

        fun init() {
            INSTANCE = GlobalDI()
        }
    }
}