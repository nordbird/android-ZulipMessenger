package ru.nordbird.tfsmessenger.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.App
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.databinding.FragmentProfileBinding
import ru.nordbird.tfsmessenger.extensions.userMessage
import ru.nordbird.tfsmessenger.ui.main.MainActivity
import ru.nordbird.tfsmessenger.ui.mvi.base.MviFragment
import ru.nordbird.tfsmessenger.ui.profile.base.ProfileAction
import ru.nordbird.tfsmessenger.ui.profile.base.ProfilePresenter
import ru.nordbird.tfsmessenger.ui.profile.base.ProfileUiEffect
import ru.nordbird.tfsmessenger.ui.profile.base.ProfileView
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserPresence
import javax.inject.Inject

class ProfileFragment : MviFragment<ProfileView, ProfileAction, ProfilePresenter>(), ProfileView {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var profilePresenter: ProfilePresenter

    override fun getPresenter(): ProfilePresenter = profilePresenter

    override fun getMviView(): ProfileView = this

    private val compositeDisposable = CompositeDisposable()
    private var userId: Int = INVALID_USER_ID
    private var isCurrentUser = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.instance.providePeopleComponent().inject(this)
    }

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initToolbar()
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

        getPresenter().input.accept(ProfileAction.LoadProfile(userId))
    }

    override fun render(state: ProfileState) {
        if (state.item == null) return

        binding.tvProfileName.text = state.item.name
        if (state.presence != null) {
            binding.tvProfileOnline.visibility = View.VISIBLE
            val (text, color) = when (state.item.presence) {
                UserPresence.ACTIVE -> {
                    getText(R.string.profile_active) to ContextCompat.getColor(requireContext(), R.color.color_profile_online)
                }
                UserPresence.IDLE -> {
                    getText(R.string.profile_idle) to ContextCompat.getColor(requireContext(), R.color.color_profile_idle)
                }
                else -> {
                    getText(R.string.profile_offline) to ContextCompat.getColor(requireContext(), R.color.color_profile_offline)
                }
            }
            binding.tvProfileOnline.text = text
            binding.tvProfileOnline.setTextColor(color)
        } else {
            binding.tvProfileOnline.visibility = View.GONE
        }

        Glide.with(this).load(state.item.avatar).into(binding.ivProfileAvatar)
        binding.sflProfile.hideShimmer()
    }

    override fun handleUiEffect(uiEffect: ProfileUiEffect) {
        when (uiEffect) {
            is ProfileUiEffect.LoadUserError -> {
                showError(uiEffect.error)
            }
        }
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
        val view = (requireActivity() as MainActivity).rootView
        Snackbar.make(view, throwable.userMessage(requireContext()), Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val PARAM_USER_ID = "param_user_id"
        private const val INVALID_USER_ID = 0
    }
}