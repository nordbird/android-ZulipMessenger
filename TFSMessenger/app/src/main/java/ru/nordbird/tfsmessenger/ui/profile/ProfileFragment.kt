package ru.nordbird.tfsmessenger.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val currentUser = DataGenerator.getCurrentUser()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        initUI()
        setupUser()
        return binding.root
    }

    private fun initUI() {
        binding.ivProfileAvatar.clipToOutline = true
    }

    private fun setupUser() {
        binding.tvProfileName.text = currentUser.name
        binding.tvProfileOnline.text = if (currentUser.isOnline) getText(R.string.profile_online) else getText(R.string.profile_offline)
        binding.tvProfileStatus.text = getText(R.string.profile_status)
        binding.ivProfileAvatar.setImageResource(R.drawable.avatar)
    }
}