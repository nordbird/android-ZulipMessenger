package ru.nordbird.tfsmessenger.di.module

import dagger.Module
import dagger.Provides
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.MessageDao
import ru.nordbird.tfsmessenger.data.dao.TopicDao
import ru.nordbird.tfsmessenger.data.repository.EventRepositoryImpl
import ru.nordbird.tfsmessenger.data.repository.MessageRepositoryImpl
import ru.nordbird.tfsmessenger.data.repository.ReactionRepositoryImpl
import ru.nordbird.tfsmessenger.data.repository.TopicRepositoryImpl
import ru.nordbird.tfsmessenger.data.repository.base.EventRepository
import ru.nordbird.tfsmessenger.data.repository.base.MessageRepository
import ru.nordbird.tfsmessenger.data.repository.base.ReactionRepository
import ru.nordbird.tfsmessenger.data.repository.base.TopicRepository
import ru.nordbird.tfsmessenger.di.scope.TopicScope
import ru.nordbird.tfsmessenger.domain.TopicInteractorImpl
import ru.nordbird.tfsmessenger.domain.base.TopicInteractor
import ru.nordbird.tfsmessenger.ui.topic.EditMessagePresenterImpl
import ru.nordbird.tfsmessenger.ui.topic.TopicPresenterImpl
import ru.nordbird.tfsmessenger.ui.topic.base.EDIT_MESSAGE_PRESENTER
import ru.nordbird.tfsmessenger.ui.topic.base.SINGLE_TOPIC_PRESENTER
import ru.nordbird.tfsmessenger.ui.topic.base.STREAM_TOPIC_PRESENTER
import ru.nordbird.tfsmessenger.ui.topic.base.TopicPresenter
import javax.inject.Named

@Module
class TopicModule {

    @TopicScope
    @Provides
    fun provideMessageRepository(apiService: ZulipService, messageDao: MessageDao): MessageRepository {
        return MessageRepositoryImpl(apiService, messageDao)
    }

    @TopicScope
    @Provides
    fun provideReactionRepository(apiService: ZulipService, messageDao: MessageDao): ReactionRepository {
        return ReactionRepositoryImpl(apiService, messageDao)
    }

    @TopicScope
    @Provides
    fun provideEventRepository(apiService: ZulipService): EventRepository {
        return EventRepositoryImpl(apiService)
    }

    @TopicScope
    @Provides
    fun provideTopicRepository(apiService: ZulipService, topicDao: TopicDao): TopicRepository {
        return TopicRepositoryImpl(apiService, topicDao)
    }

    @TopicScope
    @Provides
    fun provideTopicInteractor(
        messageRepository: MessageRepository,
        reactionRepository: ReactionRepository
    ): TopicInteractor {
        return TopicInteractorImpl(messageRepository, reactionRepository)
    }

    @TopicScope
    @Provides
    @Named(SINGLE_TOPIC_PRESENTER)
    fun provideTopicPresenter(
        topicInteractor: TopicInteractor,
        eventRepository: EventRepository,
        topicRepository: TopicRepository
    ): TopicPresenter {
        return TopicPresenterImpl(topicInteractor, eventRepository, topicRepository)
    }

    @TopicScope
    @Provides
    @Named(STREAM_TOPIC_PRESENTER)
    fun provideStreamPresenter(
        topicInteractor: TopicInteractor,
        eventRepository: EventRepository,
        topicRepository: TopicRepository
    ): TopicPresenter {
        return TopicPresenterImpl(topicInteractor, eventRepository, topicRepository)
    }

    @TopicScope
    @Provides
    @Named(EDIT_MESSAGE_PRESENTER)
    fun provideEditMessagePresenter(
        topicInteractor: TopicInteractor,
        topicRepository: TopicRepository
    ): TopicPresenter {
        return EditMessagePresenterImpl(topicInteractor, topicRepository)
    }
}