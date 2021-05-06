package ru.nordbird.tfsmessenger.ui.channels

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.App
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentChannelsBinding
import ru.nordbird.tfsmessenger.extensions.isFinishing
import ru.nordbird.tfsmessenger.ui.channels.ChannelsTabFragment.Companion.REQUEST_FILTER_QUERY
import ru.nordbird.tfsmessenger.ui.channels.ChannelsTabFragment.Companion.REQUEST_FILTER_QUERY_KEY
import ru.nordbird.tfsmessenger.ui.rx.RxSearchObservable

class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityListener: ChannelsFragmentListener
    private val compositeDisposable = CompositeDisposable()
    private var lastQuery: String = ""

    private val channelsTabs = listOf(ChannelsTabType.SUBSCRIBED, ChannelsTabType.ALL)
    private val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
        tab.text = resources.getString(channelsTabs[position].resId)
    }
    private val tabChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            sendFilterQuery(lastQuery)
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
        lastQuery = savedInstanceState?.getString(STATE_LAST_QUERY, "") ?: ""
        setHasOptionsMenu(true)
        childFragmentManager.setFragmentResultListener(REQUEST_OPEN_TOPIC, this) { _, bundle ->
            activityListener.onOpenTopic(bundle)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initToolbar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        val searchDisposable = RxSearchObservable.fromView(searchView).subscribe(::sendFilterQuery)
        compositeDisposable.add(searchDisposable)

        if (lastQuery.isNotBlank()) {
            val query = lastQuery
            searchItem.expandActionView()
            searchView.setQuery(query, true)
            searchView.clearFocus()
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_LAST_QUERY, lastQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }

    override fun onDetach() {
        if (isFinishing()) App.instance.clearChannelsComponent()
        super.onDetach()
    }

    private fun initToolbar() {
        with(activity as AppCompatActivity) {
            setSupportActionBar(binding.appbar.toolbar)
            supportActionBar?.title = getString(R.string.title_channels)
        }
    }

    private fun sendFilterQuery(query: String): Boolean {
        lastQuery = query
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
        binding.viewPager.registerOnPageChangeCallback(tabChangeListener)
        TabLayoutMediator(binding.tabs, binding.viewPager, tabConfigurationStrategy).attach()
    }

    interface ChannelsFragmentListener {
        fun onOpenTopic(bundle: Bundle)
    }

    companion object {
        private const val STATE_LAST_QUERY = "state_last_query"

        const val REQUEST_OPEN_TOPIC = "request_open_topic"
        const val REQUEST_OPEN_TOPIC_STREAM_NAME = "stream_name"
        const val REQUEST_OPEN_TOPIC_NAME = "topic_name"
        const val REQUEST_OPEN_TOPIC_COLOR_TYPE_NAME = "topic_color"
    }

}