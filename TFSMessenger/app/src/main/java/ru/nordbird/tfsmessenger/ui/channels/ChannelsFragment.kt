package ru.nordbird.tfsmessenger.ui.channels

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import ru.nordbird.tfsmessenger.databinding.FragmentChannelsBinding

class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)

        return binding.root
    }

}