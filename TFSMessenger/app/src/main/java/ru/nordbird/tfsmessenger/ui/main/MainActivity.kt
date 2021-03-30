package ru.nordbird.tfsmessenger.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.ActivityMainBinding
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment
import ru.nordbird.tfsmessenger.ui.people.PeopleFragment
import ru.nordbird.tfsmessenger.ui.profile.ProfileFragment.Companion.PARAM_USER_ID
import ru.nordbird.tfsmessenger.ui.profile.ProfileFragment.Companion.PARAM_USER_NAME
import ru.nordbird.tfsmessenger.ui.profile.ProfileFragment.Companion.PARAM_USER_ONLINE
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

class MainActivity : AppCompatActivity(), ChannelsFragment.ChannelsFragmentListener, PeopleFragment.PeopleFragmentListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private var statusBarColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        statusBarColor = window.statusBarColor
        binding.navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            onDestinationChanged(destination.id)
        }
    }

    private fun onDestinationChanged(fragmentId: Int) {
        when (fragmentId) {
            R.id.navigation_topic, R.id.navigation_profile_another -> binding.navView.visibility = View.GONE
            else -> {
                binding.navView.visibility = View.VISIBLE
                window.statusBarColor = statusBarColor
            }
        }
    }

    override fun onOpenTopic(bundle: Bundle) {
        navController.navigate(R.id.navigation_topic, bundle)
    }

    override fun onOpenUserProfile(user: UserUi) {
        navController.navigate(
            R.id.navigation_profile_another,
            bundleOf(
                PARAM_USER_ID to user.id,
                PARAM_USER_NAME to user.name,
                PARAM_USER_ONLINE to user.isOnline
            )
        )
    }
}