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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.Resource
import ru.nordbird.tfsmessenger.data.model.Status
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
import ru.nordbird.tfsmessenger.ui.recycler.holder.ErrorUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamShimmerUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TfsHolderFactory

class ChannelsTabFragment : Fragment() {

    private var _binding: FragmentChannelsTabBinding? = null
    private val binding get() = _binding!!

    private var tabType: ChannelsTabType = ChannelsTabType.ALL
    private val channelsInteractor = ChannelsInteractor
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

        val searchDisposable = when (tabType) {
            ChannelsTabType.ALL -> channelsInteractor.filterAllStreams(searchObservable)
            ChannelsTabType.SUBSCRIBED -> channelsInteractor.filterSubscribedStreams(searchObservable)
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
        initLoadStreams()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        channelsInteractor.clearDisposable()
    }

    private fun initLoadStreams() {
        showShimmer()
        val streamDisposable = when (tabType) {
            ChannelsTabType.ALL -> channelsInteractor.getAllStreams()
            ChannelsTabType.SUBSCRIBED -> channelsInteractor.getSubscribedStreams()
        }.subscribe { updateStreams(it) }

        compositeDisposable.add(streamDisposable)
    }

    private fun updateStreams(resource: Resource<List<ViewTyped>>) {
        when (resource.status) {
            Status.SUCCESS -> adapter.items = resource.data ?: emptyList()
            Status.ERROR -> adapter.items = listOf(ErrorUi())
            Status.LOADING -> showShimmer()
        }
    }

    private fun showShimmer() {
        adapter.items = listOf(StreamShimmerUi(), StreamShimmerUi(), StreamShimmerUi())
    }

    private fun initUI() {
        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.divider)
        if (dividerDrawable != null) divider.setDrawable(dividerDrawable)

        binding.rvStreams.adapter = adapter
        binding.rvStreams.addItemDecoration(divider)
    }

    private fun onStreamClick(holder: BaseViewHolder<*>) {
        when (tabType) {
            ChannelsTabType.ALL -> channelsInteractor.updateAllStreamTopics(holder.itemId)
            ChannelsTabType.SUBSCRIBED -> channelsInteractor.updateSubscribedStreamTopics(holder.itemId)
        }
    }

    private fun onTopicClick(holder: BaseViewHolder<*>) {
        val topic = when (tabType) {
            ChannelsTabType.ALL -> channelsInteractor.getAllStreamsTopic(holder.itemId)
            ChannelsTabType.SUBSCRIBED -> channelsInteractor.getSubscribedStreamsTopic(holder.itemId)
        } ?: return

        val stream = when (tabType) {
            ChannelsTabType.ALL -> channelsInteractor.getAllStream(topic.streamId)
            ChannelsTabType.SUBSCRIBED -> channelsInteractor.getSubscribedStream(topic.streamId)
        } ?: return

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
        showShimmer()
        val streamDisposable = when (tabType) {
            ChannelsTabType.ALL -> channelsInteractor.loadAllStreams()
            ChannelsTabType.SUBSCRIBED -> channelsInteractor.loadSubscribedStreams()
        }
        compositeDisposable.add(streamDisposable)
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