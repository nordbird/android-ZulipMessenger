package ru.nordbird.tfsmessenger.di

import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.dao.AppDatabaseImpl
import ru.nordbird.tfsmessenger.data.repository.UserRepository
import ru.nordbird.tfsmessenger.domain.PeopleInteractor
import ru.nordbird.tfsmessenger.ui.people.PeoplePresenter
import ru.nordbird.tfsmessenger.ui.profile.ProfilePresenter

class GlobalDI private constructor() {

    val peopleRepository by lazy { UserRepository(ZulipServiceImpl.getApi(), AppDatabaseImpl.getApi()) }

    val peopleInteractor by lazy { PeopleInteractor(peopleRepository) }

    val peoplePresenter by lazy { PeoplePresenter(peopleInteractor) }

    val profilePresenter by lazy { ProfilePresenter(peopleInteractor) }

    companion object {

        lateinit var INSTANCE: GlobalDI

        fun init() {
            INSTANCE = GlobalDI()
        }
    }
}