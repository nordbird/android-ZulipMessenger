package ru.nordbird.tfsmessenger.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.databinding.FragmentProfileBinding
import ru.nordbird.tfsmessenger.ui.people.PeopleInteractor
import ru.nordbird.tfsmessenger.ui.recycler.holder.ErrorUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserPresence
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val compositeDisposable = CompositeDisposable()
    private var userId: Int = INVALID_USER_ID
    private var isCurrentUser = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            userId = it.getInt(PARAM_USER_ID, INVALID_USER_ID)
        }

        if (userId == INVALID_USER_ID) {
            isCurrentUser = true
            userId = ZulipAuth.AUTH_ID
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        initUI()
        initToolbar()

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

        val disposable = PeopleInteractor.getUser(userId)
            .subscribe(
                { setupUser(it) },
                { showError(it) })
        compositeDisposable.add(disposable)
    }

    private fun setupUser(user: UserUi?) {
        if (user == null) return

        binding.tvProfileName.text = user.name
        val (text, color) = when (user.presence) {
            UserPresence.ACTIVE -> {
                getText(R.string.profile_active) to ContextCompat.getColor(requireContext(), R.color.color_green)
            }
            UserPresence.IDLE -> {
                getText(R.string.profile_idle) to ContextCompat.getColor(requireContext(), R.color.color_orange)
            }
            else -> {
                getText(R.string.profile_offline) to ContextCompat.getColor(requireContext(), R.color.color_red)
            }
        }
        binding.tvProfileOnline.text = text
        binding.tvProfileOnline.setTextColor(color)

        Glide.with(this).load(user.avatar).into(binding.ivProfileAvatar)
        binding.sflProfile.hideShimmer()
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

    private fun showError(throwable: Throwable) {
        Snackbar.make(binding.root, throwable.message.toString(), Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val PARAM_USER_ID = "param_user_id"
        private const val INVALID_USER_ID = 0
    }
}