package ru.nordbird.tfsmessenger.di.component

import dagger.Subcomponent
import ru.nordbird.tfsmessenger.di.module.TopicModule
import ru.nordbird.tfsmessenger.di.scope.TopicScope
import ru.nordbird.tfsmessenger.ui.topic.TopicFragment

@TopicScope
@Subcomponent(modules = [TopicModule::class])
interface TopicComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): TopicComponent
    }

    fun inject(topicFragment: TopicFragment)

}