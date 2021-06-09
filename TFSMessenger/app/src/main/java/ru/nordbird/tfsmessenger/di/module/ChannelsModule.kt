package ru.nordbird.tfsmessenger.di.module

import dagger.Module
import dagger.Provides
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.StreamDao
import ru.nordbird.tfsmessenger.data.dao.TopicDao
import ru.nordbird.tfsmessenger.data.repository.StreamRepositoryImpl
import ru.nordbird.tfsmessenger.data.repository.TopicRepositoryImpl
import ru.nordbird.tfsmessenger.data.repository.base.StreamRepository
import ru.nordbird.tfsmessenger.data.repository.base.TopicRepository
import ru.nordbird.tfsmessenger.di.scope.ChannelsScope
import ru.nordbird.tfsmessenger.domain.ChannelsInteractorImpl
import ru.nordbird.tfsmessenger.domain.base.ChannelsInteractor
import ru.nordbird.tfsmessenger.ui.channels.ChannelsPresenterImpl
import ru.nordbird.tfsmessenger.ui.channels.NewStreamPresenterImpl
import ru.nordbird.tfsmessenger.ui.channels.base.ChannelsPresenter
import ru.nordbird.tfsmessenger.ui.channels.base.NEW_STREAM_CHANNELS_PRESENTER
import ru.nordbird.tfsmessenger.ui.channels.base.STREAMS_CHANNELS_PRESENTER
import ru.nordbird.tfsmessenger.ui.channels.base.SUBSCRIPTIONS_CHANNELS_PRESENTER
import javax.inject.Named

@Module
class ChannelsModule {

    @ChannelsScope
    @Provides
    fun provideStreamRepository(apiService: ZulipService, streamDao: StreamDao): StreamRepository {
        return StreamRepositoryImpl(apiService, streamDao)
    }

    @ChannelsScope
    @Provides
    fun provideTopicRepository(apiService: ZulipService, topicDao: TopicDao): TopicRepository {
        return TopicRepositoryImpl(apiService, topicDao)
    }

    @ChannelsScope
    @Provides
    fun provideChannelsInteractor(
        streamRepository: StreamRepository,
        topicRepository: TopicRepository
    ): ChannelsInteractor {
        return ChannelsInteractorImpl(streamRepository, topicRepository)
    }

    @ChannelsScope
    @Provides
    @Named(STREAMS_CHANNELS_PRESENTER)
    fun provideStreamsChannelsPresenter(channelsInteractor: ChannelsInteractor): ChannelsPresenter {
        return ChannelsPresenterImpl(channelsInteractor)
    }

    @ChannelsScope
    @Provides
    @Named(SUBSCRIPTIONS_CHANNELS_PRESENTER)
    fun provideSubscriptionsChannelsPresenter(channelsInteractor: ChannelsInteractor): ChannelsPresenter {
        return ChannelsPresenterImpl(channelsInteractor)
    }

    @ChannelsScope
    @Provides
    @Named(NEW_STREAM_CHANNELS_PRESENTER)
    fun provideNewStreamPresenter(channelsInteractor: ChannelsInteractor): ChannelsPresenter {
        return NewStreamPresenterImpl(channelsInteractor)
    }

}