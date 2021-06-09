package ru.nordbird.tfsmessenger.di.component

import dagger.Subcomponent
import ru.nordbird.tfsmessenger.di.module.PeopleModule
import ru.nordbird.tfsmessenger.di.scope.PeopleScope
import ru.nordbird.tfsmessenger.ui.people.PeopleFragment
import ru.nordbird.tfsmessenger.ui.profile.ProfileFragment

@PeopleScope
@Subcomponent(modules = [PeopleModule::class])
interface PeopleComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): PeopleComponent
    }

    fun inject(peopleFragment: PeopleFragment)

    fun inject(profileFragment: ProfileFragment)

}