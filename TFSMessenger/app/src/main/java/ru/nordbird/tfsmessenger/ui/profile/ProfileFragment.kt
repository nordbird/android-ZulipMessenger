package ru.nordbird.tfsmessenger.ui.profile

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var userId = ""
    private var userName = ""
    private var userOnline = false
    private var isCurrentUser = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val currentUser = DataGenerator.getCurrentUser()
        userId = currentUser.id
        userName = currentUser.name
        userOnline = currentUser.isOnline
        arguments?.let {
            userId = it.getString(PARAM_USER_ID, "")
            userName = it.getString(PARAM_USER_NAME, "")
            userOnline = it.getBoolean(PARAM_USER_ONLINE)
        }
        isCurrentUser = userId == currentUser.id
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

    private fun initUI() {
        binding.ivProfileAvatar.clipToOutline = true
    }

    private fun setupUser() {
        binding.btnLogout.visibility = if (isCurrentUser) View.VISIBLE else View.GONE
        binding.tvProfileName.text = userName
        binding.tvProfileOnline.text = if (userOnline) getText(R.string.profile_online) else getText(R.string.profile_offline)
        binding.tvProfileOnline.setTextColor(if (userOnline) Color.GREEN else Color.RED)
        binding.tvProfileStatus.text = getText(R.string.profile_status)
        binding.ivProfileAvatar.setImageResource(R.drawable.avatar)
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
        const val PARAM_USER_ID = "param_user_id"
        const val PARAM_USER_NAME = "param_user_name"
        const val PARAM_USER_ONLINE = "param_user_online"
    }
}