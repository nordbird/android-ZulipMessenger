package ru.nordbird.tfsmessenger.ui.channels

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChannelsFragmentStateAdapter(
    fragment: Fragment,
    private val items: List<ChannelsTabType>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return ChannelsTabFragment.newInstance(items[position])
    }

}