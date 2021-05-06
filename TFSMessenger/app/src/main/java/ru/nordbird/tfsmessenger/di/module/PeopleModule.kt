package ru.nordbird.tfsmessenger.di.module

import dagger.Module
import dagger.Provides
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.UserDao
import ru.nordbird.tfsmessenger.data.repository.UserRepositoryImpl
import ru.nordbird.tfsmessenger.data.repository.base.UserRepository
import ru.nordbird.tfsmessenger.di.scope.PeopleScope
import ru.nordbird.tfsmessenger.domain.PeopleInteractorImpl
import ru.nordbird.tfsmessenger.domain.base.PeopleInteractor
import ru.nordbird.tfsmessenger.ui.people.PeoplePresenterImpl
import ru.nordbird.tfsmessenger.ui.people.base.PeoplePresenter
import ru.nordbird.tfsmessenger.ui.profile.ProfilePresenterImpl
import ru.nordbird.tfsmessenger.ui.profile.base.ProfilePresenter

@Module
class PeopleModule {

    @PeopleScope
    @Provides
    fun provideUserRepository(apiService: ZulipService, userDao: UserDao): UserRepository {
        return UserRepositoryImpl(apiService, userDao)
    }

    @PeopleScope
    @Provides
    fun providePeopleInteractor(userRepository: UserRepository): PeopleInteractor {
        return PeopleInteractorImpl(userRepository)
    }

    @PeopleScope
    @Provides
    fun providePeoplePresenter(peopleInteractor: PeopleInteractor): PeoplePresenter {
        return PeoplePresenterImpl(peopleInteractor)
    }

    @PeopleScope
    @Provides
    fun provideProfilePresenter(peopleInteractor: PeopleInteractor): ProfilePresenter {
        return ProfilePresenterImpl(peopleInteractor)
    }
}