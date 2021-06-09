package ru.nordbird.tfsmessenger.di.component

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.nordbird.tfsmessenger.di.module.DatabaseModule
import ru.nordbird.tfsmessenger.di.module.NetworkModule
import ru.nordbird.tfsmessenger.di.scope.AppScope
import ru.nordbird.tfsmessenger.ui.main.MainActivity

@AppScope
@Component(modules = [NetworkModule::class, DatabaseModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(mainActivity: MainActivity)

    fun channelsComponent(): ChannelsComponent.Factory

    fun peopleComponent(): PeopleComponent.Factory

    fun topicComponent(): TopicComponent.Factory

}