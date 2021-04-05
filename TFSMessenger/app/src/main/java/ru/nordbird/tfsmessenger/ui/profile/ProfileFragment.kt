package ru.nordbird.tfsmessenger.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentProfileBinding
import ru.nordbird.tfsmessenger.ui.people.PeopleInteractor
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserPresence
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val compositeDisposable = CompositeDisposable()
    private var user: UserUi? = null
    private var isCurrentUser = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            user = it.getParcelable(PARAM_USER)
        }
        isCurrentUser = user == null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        initUI()
        initToolbar()
        setupUser()
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            activity?.onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }

    private fun initUI() {
        binding.ivProfileAvatar.clipToOutline = true

        if (isCurrentUser) {
            val disposable = PeopleInteractor.getUser("").subscribe {
                user = it
                setupUser()
            }
            compositeDisposable.add(disposable)
        }
    }

    private fun setupUser() {
        if (user == null) return
        binding.tvProfileName.text = user?.name
        when (user?.presence) {
            UserPresence.ACTIVE -> {
                binding.tvProfileOnline.text = getText(R.string.profile_active)
                binding.tvProfileOnline.setTextColor(resources.getColor(R.color.color_green, context?.theme))
            }
            UserPresence.IDLE -> {
                binding.tvProfileOnline.text = getText(R.string.profile_idle)
                binding.tvProfileOnline.setTextColor(resources.getColor(R.color.color_orange, context?.theme))
            }
            UserPresence.OFFLINE -> {
                binding.tvProfileOnline.text = getText(R.string.profile_offline)
                binding.tvProfileOnline.setTextColor(resources.getColor(R.color.color_red, context?.theme))
            }
        }
        binding.tvProfileStatus.text = getText(R.string.profile_status)

        Glide.with(this).load(user?.avatar).into(binding.ivProfileAvatar)
    }

    private fun initToolbar() {
        if (isCurrentUser) {
            binding.appbar.toolbar.visibility = View.GONE
        } else {
            binding.appbar.toolbar.visibility = View.VISIBLE
            with(activity as AppCompatActivity) {
                setSupportActionBar(binding.appbar.toolbar)
                supportActionBar?.title = getString(R.string.title_profile)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }


    companion object {
        const val PARAM_USER = "param_user"
    }
}