package ru.nordbird.tfsmessenger.di.module

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import ru.nordbird.tfsmessenger.data.api.ZulipClientFactory
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.api.ZulipServiceFactory
import ru.nordbird.tfsmessenger.di.scope.AppScope

@Module
class NetworkModule {

    @Provides
    @AppScope
    fun provideApiClient(): OkHttpClient {
        return ZulipClientFactory.create()
    }

    @Provides
    @AppScope
    fun provideApiService(client: OkHttpClient): ZulipService {
        return ZulipServiceFactory.create(client)
    }

}