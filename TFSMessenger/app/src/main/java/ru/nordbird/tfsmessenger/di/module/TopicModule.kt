package ru.nordbird.tfsmessenger.di.module

import dagger.Module
import dagger.Provides
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.MessageDao
import ru.nordbird.tfsmessenger.data.repository.EventRepositoryImpl
import ru.nordbird.tfsmessenger.data.repository.MessageRepositoryImpl
import ru.nordbird.tfsmessenger.data.repository.ReactionRepositoryImpl
import ru.nordbird.tfsmessenger.data.repository.base.EventRepository
import ru.nordbird.tfsmessenger.data.repository.base.MessageRepository
import ru.nordbird.tfsmessenger.data.repository.base.ReactionRepository
import ru.nordbird.tfsmessenger.di.scope.TopicScope
import ru.nordbird.tfsmessenger.domain.TopicInteractorImpl
import ru.nordbird.tfsmessenger.domain.base.TopicInteractor
import ru.nordbird.tfsmessenger.ui.topic.TopicPresenterImpl
import ru.nordbird.tfsmessenger.ui.topic.base.TopicPresenter

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
    fun provideTopicInteractor(
        messageRepository: MessageRepository,
        reactionRepository: ReactionRepository
    ): TopicInteractor {
        return TopicInteractorImpl(messageRepository, reactionRepository)
    }

    @TopicScope
    @Provides
    fun provideTopicPresenter(
        topicInteractor: TopicInteractor,
        eventRepository: EventRepository
    ): TopicPresenter {
        return TopicPresenterImpl(topicInteractor, eventRepository)
    }
}