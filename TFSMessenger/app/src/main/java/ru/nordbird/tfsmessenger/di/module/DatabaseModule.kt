package ru.nordbird.tfsmessenger.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.nordbird.tfsmessenger.data.dao.*
import ru.nordbird.tfsmessenger.di.scope.AppScope

@Module
class DatabaseModule {

    @Provides
    @AppScope
    fun provideAppDatabase(context: Context): AppDatabase {
        return AppDatabaseFactory.create(context)
    }

    @Provides
    @AppScope
    fun provideMessageDao(appDatabase: AppDatabase): MessageDao {
        return appDatabase.messageDao()
    }

    @Provides
    @AppScope
    fun provideStreamDao(appDatabase: AppDatabase): StreamDao {
        return appDatabase.streamDao()
    }

    @Provides
    @AppScope
    fun provideTopicDao(appDatabase: AppDatabase): TopicDao {
        return appDatabase.topicDao()
    }

    @Provides
    @AppScope
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

}