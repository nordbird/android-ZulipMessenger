package ru.nordbird.tfsmessenger.ui.channels

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var activityListener: ChannelsFragmentListener

    private val channelsTabs = listOf(ChannelsTabType.SUBSCRIBED, ChannelsTabType.ALL)
    private val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ChannelsFragmentListener) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement ChannelsFragmentListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        childFragmentManager.setFragmentResultListener(REQUEST_OPEN_TOPIC, this) { _, bundle ->
            activityListener.onOpenTopic(bundle)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

    override fun onStart() {
        super.onStart()
        initToolbar()
    }

    private fun initToolbar() {
        with(activity as AppCompatActivity) {
            setSupportActionBar(binding.appbar.toolbar)
            supportActionBar?.title = getString(R.string.title_channels)
        }
    }

    private fun sendFilterQuery(query: String?): Boolean {
        val requestKey = REQUEST_FILTER_QUERY + channelsTabs[binding.viewPager.currentItem]
        childFragmentManager.setFragmentResult(
            requestKey,
            bundleOf(REQUEST_FILTER_QUERY_KEY to query)
        )
        return true
    }

    private fun initUI() {
        val adapter = ChannelsFragmentStateAdapter(this, channelsTabs)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.viewPager, tabConfigurationStrategy).attach()
    }

    interface ChannelsFragmentListener {
        fun onOpenTopic(bundle: Bundle)
    }

    companion object {

        const val REQUEST_OPEN_TOPIC = "request_open_topic"
        const val REQUEST_OPEN_TOPIC_STREAM_NAME = "stream_name"
        const val REQUEST_OPEN_TOPIC_NAME = "topic_name"
        const val REQUEST_OPEN_TOPIC_COLOR = "topic_color"
    }

}