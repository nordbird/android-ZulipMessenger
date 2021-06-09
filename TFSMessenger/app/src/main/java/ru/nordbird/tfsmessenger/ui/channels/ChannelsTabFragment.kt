package ru.nordbird.tfsmessenger.ui.channels

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.App
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentChannelsTabBinding
import ru.nordbird.tfsmessenger.extensions.userMessage
import ru.nordbird.tfsmessenger.ui.channels.base.*
import ru.nordbird.tfsmessenger.ui.main.MainActivity
import ru.nordbird.tfsmessenger.ui.mvi.base.MviFragment
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.*
import ru.nordbird.tfsmessenger.ui.recycler.holder.*
import ru.nordbird.tfsmessenger.ui.topic.TopicFragment.Companion.OPEN_TOPIC_COLOR_TYPE_NAME
import ru.nordbird.tfsmessenger.ui.topic.TopicFragment.Companion.OPEN_TOPIC_NAME
import ru.nordbird.tfsmessenger.ui.topic.TopicFragment.Companion.OPEN_TOPIC_STREAM_ID
import ru.nordbird.tfsmessenger.ui.topic.TopicFragment.Companion.OPEN_TOPIC_STREAM_NAME

class ChannelsTabFragment : MviFragment<ChannelsView, ChannelsAction, ChannelsPresenter>(), ChannelsView {

    private var _binding: FragmentChannelsTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityListener: ChannelsTabFragmentListener

    lateinit var channelsPresenter: ChannelsPresenter

    private var tabType: ChannelsTabType = ChannelsTabType.ALL
    private val compositeDisposable = CompositeDisposable()

    private val clickListener: ViewHolderClickListener = object : ViewHolderClickListener {
        override fun onViewHolderClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType?) {
            when (holder.itemViewType) {
                R.layout.item_stream -> onStreamClick(holder, clickType)
                R.layout.item_topic -> onTopicClick(holder)
                R.layout.item_error -> onReloadClick()
            }
        }

        override fun onViewHolderLongClick(holder: BaseViewHolder<*>, view: View): Boolean = true
    }

    private val holderFactory = TfsHolderFactory(clickListener = clickListener)
    private val diffUtilCallback = DiffUtilCallback<ViewTyped>()
    private val adapter = Adapter(holderFactory, diffUtilCallback)
    private var needScroll: Boolean = false

    override fun getPresenter(): ChannelsPresenter = channelsPresenter

    override fun getMviView(): ChannelsView = this

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ChannelsTabFragmentListener) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement ChannelsTabFragmentListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (arguments?.getInt(ARG_TAB_TYPE)) {
            ChannelsTabType.ALL.ordinal -> tabType = ChannelsTabType.ALL
            ChannelsTabType.SUBSCRIBED.ordinal -> tabType = ChannelsTabType.SUBSCRIBED
        }
        channelsPresenter = when (tabType) {
            ChannelsTabType.ALL -> App.instance.provideChannelsComponent().provideStreamsChannelsPresenter()
            ChannelsTabType.SUBSCRIBED -> App.instance.provideChannelsComponent().provideSubscriptionsChannelsPresenter()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChannelsTabBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        loadStreams()

        val requestKey = REQUEST_FILTER_QUERY + tabType
        setFragmentResultListener(requestKey) { _, bundle ->
            filterStreams(bundle.getString(REQUEST_FILTER_QUERY_KEY, ""))
        }
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }

    override fun render(state: ChannelsState) {
        needScroll = state.needScroll
        adapter.items = state.items
    }

    override fun handleUiEffect(uiEffect: ChannelsUiEffect) {
        when (uiEffect) {
            is ChannelsUiEffect.ActionError -> {
                showError(uiEffect.error)
            }
        }
    }

    private fun showError(throwable: Throwable) {
        val view = (requireActivity() as MainActivity).rootView
        Snackbar.make(view, throwable.userMessage(requireContext()), Snackbar.LENGTH_SHORT).show()
    }

    private fun initUI() {
        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.divider)
        if (dividerDrawable != null) divider.setDrawable(dividerDrawable)

        binding.rvStreams.adapter = adapter
        binding.rvStreams.addItemDecoration(divider)

        val disposable = adapter.updateAction.subscribe {
            if (needScroll) {
                binding.rvStreams.layoutManager?.scrollToPosition(0)
            }
        }
        compositeDisposable.add(disposable)
    }

    private fun onStreamClick(holder: BaseViewHolder<*>, clickType: ViewHolderClickType?) {
        val stream = adapter.items[holder.absoluteAdapterPosition] as StreamUi

        when (clickType) {
            StreamVHClickType.TOGGLE_TOPICS_CLICK -> {
                if (stream.topicExpanded) {
                    getPresenter().input.accept(ChannelsAction.CollapseTopics(stream.name))
                } else {
                    getPresenter().input.accept(ChannelsAction.ExpandTopics(stream.id, stream.name))
                }
            }
            StreamVHClickType.OPEN_STREAM_CLICK -> openStream(stream)
        }
    }

    private fun openStream(stream: StreamUi) {
        activityListener.onOpenStream(
            bundleOf(
                OPEN_TOPIC_STREAM_ID to stream.id,
                OPEN_TOPIC_STREAM_NAME to stream.name
            )
        )
    }

    private fun onTopicClick(holder: BaseViewHolder<*>) {
        val topic = adapter.items[holder.absoluteAdapterPosition] as TopicUi
        val stream = getStream(topic.streamName) ?: return

        activityListener.onOpenTopic(
            bundleOf(
                OPEN_TOPIC_STREAM_ID to stream.id,
                OPEN_TOPIC_STREAM_NAME to stream.name,
                OPEN_TOPIC_NAME to topic.name,
                OPEN_TOPIC_COLOR_TYPE_NAME to topic.colorType.name
            )
        )
    }

    private fun loadStreams() {
        when (tabType) {
            ChannelsTabType.ALL -> getPresenter().input.accept(ChannelsAction.LoadStreams)
            ChannelsTabType.SUBSCRIBED -> getPresenter().input.accept(ChannelsAction.LoadSubscriptions)
        }
    }

    private fun filterStreams(query: String) {
        getPresenter().input.accept(ChannelsAction.FilterStreams(query))
    }

    private fun onReloadClick() {
        loadStreams()
    }

    private fun getStream(streamName: String): StreamUi? {
        return adapter.items.filterIsInstance<StreamUi>().firstOrNull { it.name == streamName }
    }

    interface ChannelsTabFragmentListener {

        fun onOpenStream(bundle: Bundle)

        fun onOpenTopic(bundle: Bundle)
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