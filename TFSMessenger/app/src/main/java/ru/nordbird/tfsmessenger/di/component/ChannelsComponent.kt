package ru.nordbird.tfsmessenger.di.component

import dagger.Subcomponent
import ru.nordbird.tfsmessenger.di.module.ChannelsModule
import ru.nordbird.tfsmessenger.di.scope.ChannelsScope
import ru.nordbird.tfsmessenger.ui.channels.ChannelsTabFragment
import ru.nordbird.tfsmessenger.ui.channels.NewStreamFragment

@ChannelsScope
@Subcomponent(modules = [ChannelsModule::class])
interface ChannelsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ChannelsComponent
    }

    fun inject(channelsTabFragment: ChannelsTabFragment)

    fun inject(newStreamFragment: NewStreamFragment)

}