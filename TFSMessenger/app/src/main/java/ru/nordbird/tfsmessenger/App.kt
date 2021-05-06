package ru.nordbird.tfsmessenger

import android.app.Application
import ru.nordbird.tfsmessenger.di.component.*

class App : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }

    private var channelsComponent: ChannelsComponent? = null
    private var peopleComponent: PeopleComponent? = null
    private var topicComponent: TopicComponent? = null

    fun provideChannelsComponent(): ChannelsComponent {
        if (channelsComponent == null) {
            channelsComponent = appComponent.channelsComponent().create()
        }
        return channelsComponent!!
    }

    fun clearChannelsComponent() {
        channelsComponent = null
    }

    fun providePeopleComponent(): PeopleComponent {
        if (peopleComponent == null) {
            peopleComponent = appComponent.peopleComponent().create()
        }
        return peopleComponent!!
    }

    fun clearPeopleComponent() {
        peopleComponent = null
    }

    fun provideTopicComponent(): TopicComponent {
        if (topicComponent == null) {
            topicComponent = appComponent.topicComponent().create()
        }
        return topicComponent!!
    }

    fun clearTopicComponent() {
        topicComponent = null
    }

    companion object {
        lateinit var instance: App
    }

    init {
        instance = this
    }
}