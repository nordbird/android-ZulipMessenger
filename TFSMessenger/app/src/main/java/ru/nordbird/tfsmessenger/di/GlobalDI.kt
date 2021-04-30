package ru.nordbird.tfsmessenger.di

import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.dao.AppDatabaseImpl
import ru.nordbird.tfsmessenger.data.repository.*
import ru.nordbird.tfsmessenger.domain.ChannelsInteractor
import ru.nordbird.tfsmessenger.domain.PeopleInteractor
import ru.nordbird.tfsmessenger.domain.TopicInteractor
import ru.nordbird.tfsmessenger.ui.channels.ChannelsPresenter
import ru.nordbird.tfsmessenger.ui.channels.ChannelsTabType
import ru.nordbird.tfsmessenger.ui.people.PeoplePresenter
import ru.nordbird.tfsmessenger.ui.profile.ProfilePresenter
import ru.nordbird.tfsmessenger.ui.topic.TopicPresenter

class GlobalDI private constructor() {

    private val peopleRepository by lazy { UserRepository(ZulipServiceImpl.getApi(), AppDatabaseImpl.userDao()) }
    private val peopleInteractor by lazy { PeopleInteractor(peopleRepository) }
    val peoplePresenter by lazy { PeoplePresenter(peopleInteractor) }
    val profilePresenter by lazy { ProfilePresenter(peopleInteractor) }

    private val streamRepository by lazy { StreamRepository(ZulipServiceImpl.getApi(), AppDatabaseImpl.streamDao()) }
    private val topicRepository by lazy { TopicRepository(ZulipServiceImpl.getApi(), AppDatabaseImpl.topicDao()) }
    private val subscriptionInteractor by lazy { ChannelsInteractor(ChannelsTabType.SUBSCRIBED, streamRepository, topicRepository, messageRepository) }
    private val streamInteractor by lazy { ChannelsInteractor(ChannelsTabType.ALL, streamRepository, topicRepository, messageRepository) }
    val subscriptionPresenter by lazy { ChannelsPresenter(subscriptionInteractor) }
    val streamPresenter by lazy { ChannelsPresenter(streamInteractor) }

    private val messageRepository by lazy { MessageRepository(ZulipServiceImpl.getApi(), AppDatabaseImpl.messageDao()) }
    private val reactionRepository by lazy { ReactionRepository(ZulipServiceImpl.getApi(), AppDatabaseImpl.messageDao()) }
    private val topicInteractor by lazy { TopicInteractor(messageRepository, reactionRepository) }
    val topicPresenter by lazy { TopicPresenter(topicInteractor) }

    companion object {

        lateinit var INSTANCE: GlobalDI

        fun init() {
            INSTANCE = GlobalDI()
        }
    }
}