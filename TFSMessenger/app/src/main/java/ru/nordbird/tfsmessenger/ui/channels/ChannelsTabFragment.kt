package ru.nordbird.tfsmessenger.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentChannelsTabBinding
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_COLOR
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_NAME
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_STREAM_ID
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_STREAM_NAME
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.*

class ChannelsTabFragment : Fragment() {

    private var _binding: FragmentChannelsTabBinding? = null
    private val binding get() = _binding!!

    private var tabType: ChannelsTabType = ChannelsTabType.ALL
    private lateinit var channelsInteractor: ChannelsInteractor
    private val compositeDisposable = CompositeDisposable()
    private val searchObservable: BehaviorSubject<String> = BehaviorSubject.create()

    private val clickListener: ViewHolderClickListener = object : ViewHolderClickListener {
        override fun onViewHolderClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType?) {
            when (holder.itemViewType) {
                R.layout.item_stream -> onStreamClick(holder)
                R.layout.item_topic -> onTopicClick(holder)
                R.layout.item_error -> onReloadClick()
            }
        }

        override fun onViewHolderLongClick(holder: BaseViewHolder<*>, view: View): Boolean = true
    }

    private val holderFactory = TfsHolderFactory(clickListener = clickListener)
    private val adapter = Adapter<ViewTyped>(holderFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (arguments?.getInt(ARG_TAB_TYPE)) {
            ChannelsTabType.ALL.ordinal -> tabType = ChannelsTabType.ALL
            ChannelsTabType.SUBSCRIBED.ordinal -> tabType = ChannelsTabType.SUBSCRIBED
        }

        channelsInteractor = ChannelsInteractor(tabType)

        val searchDisposable = when (tabType) {
            ChannelsTabType.ALL -> channelsInteractor.filterStreams(searchObservable)
            ChannelsTabType.SUBSCRIBED -> channelsInteractor.filterStreams(searchObservable)
        }.subscribe {
            adapter.items = it
            binding.rvStreams.layoutManager?.scrollToPosition(0)
        }

        compositeDisposable.add(searchDisposable)

        val requestKey = REQUEST_FILTER_QUERY + tabType
        setFragmentResultListener(requestKey) { _, bundle ->
            filterStreams(bundle.getString(REQUEST_FILTER_QUERY_KEY, ""))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChannelsTabBinding.inflate(inflater, container, false)
        initUI()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun updateStreams() {
        showShimmer()
        val streamDisposable = channelsInteractor.getStreams()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { adapter.items = it },
                { showError(it) }
            )

        compositeDisposable.add(streamDisposable)
    }

    private fun showShimmer() {
        adapter.items = listOf(StreamShimmerUi(), StreamShimmerUi(), StreamShimmerUi())
    }

    private fun showError(throwable: Throwable) {
        adapter.items = listOf(ErrorUi())
        Snackbar.make(binding.root, throwable.message.toString(), Snackbar.LENGTH_SHORT).show()
    }

    private fun initUI() {
        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.divider)
        if (dividerDrawable != null) divider.setDrawable(dividerDrawable)

        binding.rvStreams.adapter = adapter
        binding.rvStreams.addItemDecoration(divider)

        updateStreams()
    }

    private fun onStreamClick(holder: BaseViewHolder<*>) {
        val streamDisposable = channelsInteractor.updateStreamTopics(holder.itemId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { adapter.items = it },
                { showError(it) }
            )

        compositeDisposable.add(streamDisposable)
    }

    private fun onTopicClick(holder: BaseViewHolder<*>) {
        val topic = adapter.items[holder.absoluteAdapterPosition] as TopicUi
        val stream = getStream(topic.streamId) ?: return

        setFragmentResult(
            REQUEST_OPEN_TOPIC,
            bundleOf(
                REQUEST_OPEN_TOPIC_STREAM_ID to stream.id,
                REQUEST_OPEN_TOPIC_STREAM_NAME to stream.name,
                REQUEST_OPEN_TOPIC_NAME to topic.name,
                REQUEST_OPEN_TOPIC_COLOR to topic.color
            )
        )
    }

    private fun filterStreams(query: String) {
        searchObservable.onNext(query)
    }

    private fun onReloadClick() {
        updateStreams()
    }

    private fun getStream(streamId: String): StreamUi? {
        return adapter.items.filterIsInstance<StreamUi>().firstOrNull { it.id == streamId }
    }

    companion object {

        const val REQUEST_FILTER_QUERY = "request_filter_query"
        const val REQUEST_FILTER_QUERY_KEY = "query"

        private const val ARG_TAB_TYPE = "title_res_id"

        @JvmStatic
        fun newInstance(tabType: ChannelsTabType): ChannelsTabFragment {
            return ChannelsTabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAB_TYPE, tabType.ordinal)
                }
            }
        }
    }
}

enum class ChannelsTabType(val resId: Int) {
    SUBSCRIBED(R.string.tab_channels_subscribed),
    ALL(R.string.tab_channels_all)
}