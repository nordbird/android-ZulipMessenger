package ru.nordbird.tfsmessenger.ui.channels

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentChannelsBinding
import ru.nordbird.tfsmessenger.ui.channels.ChannelsTabFragment.Companion.REQUEST_FILTER_QUERY
import ru.nordbird.tfsmessenger.ui.channels.ChannelsTabFragment.Companion.REQUEST_FILTER_QUERY_KEY

class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!

    private val channelsTabs = listOf(ChannelsTabType.SUBSCRIBED, ChannelsTabType.ALL)
    private val tabConfigurationStrategy =
        TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            tab.text = resources.getString(channelsTabs[position].resId)
        }
    private val searchListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return sendFilterQuery(query)
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return sendFilterQuery(newText)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        initUI()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(searchListener)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun sendFilterQuery(query: String?): Boolean {
        childFragmentManager.setFragmentResult(
            REQUEST_FILTER_QUERY + channelsTabs[binding.viewPager.currentItem],
            bundleOf(REQUEST_FILTER_QUERY_KEY to query)
        )
        return true
    }

    private fun initUI() {
        val adapter = ChannelsFragmentStateAdapter(this, channelsTabs)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.viewPager, tabConfigurationStrategy).attach()
    }

}