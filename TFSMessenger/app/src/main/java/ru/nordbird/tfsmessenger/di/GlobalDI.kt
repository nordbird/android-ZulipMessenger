package ru.nordbird.tfsmessenger.di

import android.content.Context
import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.dao.AppDatabaseImpl
import ru.nordbird.tfsmessenger.data.repository.*
import ru.nordbird.tfsmessenger.domain.ChannelsInteractorImpl
import ru.nordbird.tfsmessenger.domain.PeopleInteractorImpl
import ru.nordbird.tfsmessenger.domain.TopicInteractorImpl
import ru.nordbird.tfsmessenger.ui.channels.ChannelsPresenter
import ru.nordbird.tfsmessenger.ui.channels.ChannelsTabType
import ru.nordbird.tfsmessenger.ui.people.PeoplePresenter
import ru.nordbird.tfsmessenger.ui.profile.ProfilePresenter
import ru.nordbird.tfsmessenger.ui.topic.TopicPresenter
import ru.nordbird.tfsmessenger.utils.network.RxConnectionObservable

class GlobalDI private constructor(context: Context) {

    val connectionState by lazy { RxConnectionObservable(context) }

    private val peopleRepository by lazy { UserRepositoryImpl(ZulipServiceImpl.getApi(), AppDatabaseImpl.userDao()) }
    private val peopleInteractor by lazy { PeopleInteractorImpl(peopleRepository) }
    val peoplePresenter by lazy { PeoplePresenter(peopleInteractor) }
    val profilePresenter by lazy { ProfilePresenter(peopleInteractor) }

    private val streamRepository by lazy { StreamRepositoryImpl(ZulipServiceImpl.getApi(), AppDatabaseImpl.streamDao()) }
    private val topicRepository by lazy { TopicRepositoryImpl(ZulipServiceImpl.getApi(), AppDatabaseImpl.topicDao()) }
    private val subscriptionInteractor by lazy { ChannelsInteractorImpl(ChannelsTabType.SUBSCRIBED, streamRepository, topicRepository, messageRepository) }
    private val streamInteractor by lazy { ChannelsInteractorImpl(ChannelsTabType.ALL, streamRepository, topicRepository, messageRepository) }
    val subscriptionPresenter by lazy { ChannelsPresenter(subscriptionInteractor) }
    val streamPresenter by lazy { ChannelsPresenter(streamInteractor) }

    private val eventRepository by lazy { EventRepositoryImpl(ZulipServiceImpl.getApi()) }

    private val messageRepository by lazy { MessageRepositoryImpl(ZulipServiceImpl.getApi(), AppDatabaseImpl.messageDao()) }
    private val reactionRepository by lazy { ReactionRepositoryImpl(ZulipServiceImpl.getApi(), AppDatabaseImpl.messageDao()) }
    private val topicInteractor by lazy { TopicInteractorImpl(messageRepository, reactionRepository) }
    val topicPresenter by lazy { TopicPresenter(topicInteractor, eventRepository) }

    companion object {

        lateinit var INSTANCE: GlobalDI

        fun init(context: Context) {
            INSTANCE = GlobalDI(context)
        }
    }
}