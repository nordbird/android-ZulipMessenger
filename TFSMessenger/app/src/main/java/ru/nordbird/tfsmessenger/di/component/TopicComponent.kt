package ru.nordbird.tfsmessenger.di.component

import dagger.Subcomponent
import ru.nordbird.tfsmessenger.di.module.TopicModule
import ru.nordbird.tfsmessenger.di.scope.TopicScope
import ru.nordbird.tfsmessenger.ui.topic.EditMessageFragment
import ru.nordbird.tfsmessenger.ui.topic.base.SINGLE_TOPIC_PRESENTER
import ru.nordbird.tfsmessenger.ui.topic.base.STREAM_TOPIC_PRESENTER
import ru.nordbird.tfsmessenger.ui.topic.base.TopicPresenter
import javax.inject.Named

@TopicScope
@Subcomponent(modules = [TopicModule::class])
interface TopicComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): TopicComponent
    }

    fun inject(editMessageFragment: EditMessageFragment)

    @Named(SINGLE_TOPIC_PRESENTER)
    fun provideTopicPresenter(): TopicPresenter

    @Named(STREAM_TOPIC_PRESENTER)
    fun provideStreamPresenter(): TopicPresenter

}