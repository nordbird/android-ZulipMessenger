package ru.nordbird.tfsmessenger.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.App
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.ActivityMainBinding
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment
import ru.nordbird.tfsmessenger.ui.people.PeopleFragment
import ru.nordbird.tfsmessenger.ui.profile.ProfileFragment.Companion.PARAM_USER_ID
import ru.nordbird.tfsmessenger.utils.network.RxConnectionObservable
import javax.inject.Inject

class MainActivity : AppCompatActivity(), ChannelsFragment.ChannelsFragmentListener, PeopleFragment.PeopleFragmentListener {

    val rootView: View get() = binding.root

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private var statusBarColor: Int = 0

    @Inject
    lateinit var connectionObservable: RxConnectionObservable

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setContentView(binding.root)
        initUI()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun initUI() {
        statusBarColor = window.statusBarColor
        binding.navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            onDestinationChanged(destination.id)
        }

        val disposable = connectionObservable.networkState.subscribe(::showConnectionInfo)
        compositeDisposable.add(disposable)
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

    private fun showConnectionInfo(isConnected: Boolean) {
        if (!isConnected) {
            Toast.makeText(this, getString(R.string.error_connection_lost), Toast.LENGTH_LONG).show()
        }
    }

    override fun onOpenTopic(bundle: Bundle) {
        navController.navigate(R.id.navigation_topic, bundle)
    }

    override fun onOpenUserProfile(userId: Int) {
        navController.navigate(
            R.id.navigation_profile_another,
            bundleOf(PARAM_USER_ID to userId)
        )
    }
}