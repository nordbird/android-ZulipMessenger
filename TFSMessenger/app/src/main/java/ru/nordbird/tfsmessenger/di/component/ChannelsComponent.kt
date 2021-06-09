package ru.nordbird.tfsmessenger.di.component

import dagger.Subcomponent
import ru.nordbird.tfsmessenger.di.module.ChannelsModule
import ru.nordbird.tfsmessenger.di.scope.ChannelsScope
import ru.nordbird.tfsmessenger.ui.channels.NewStreamFragment
import ru.nordbird.tfsmessenger.ui.channels.base.ChannelsPresenter
import ru.nordbird.tfsmessenger.ui.channels.base.STREAMS_CHANNELS_PRESENTER
import ru.nordbird.tfsmessenger.ui.channels.base.SUBSCRIPTIONS_CHANNELS_PRESENTER
import javax.inject.Named

@ChannelsScope
@Subcomponent(modules = [ChannelsModule::class])
interface ChannelsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ChannelsComponent
    }

    @Named(STREAMS_CHANNELS_PRESENTER)
    fun provideStreamsChannelsPresenter(): ChannelsPresenter

    @Named(SUBSCRIPTIONS_CHANNELS_PRESENTER)
    fun provideSubscriptionsChannelsPresenter(): ChannelsPresenter

    fun inject(newStreamFragment: NewStreamFragment)

}