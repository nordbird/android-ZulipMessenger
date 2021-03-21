package ru.nordbird.tfsmessenger.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.DividerItemDecoration
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentChannelsTabBinding
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.TfsHolderFactory

class ChannelsTabFragment : Fragment() {

    private var _binding: FragmentChannelsTabBinding? = null
    private val binding get() = _binding!!

    private var tabType: ChannelsTabType = ChannelsTabType.ALL
    private val channelsInteractor = ChannelsInteractor

    private val clickListener: ViewHolderClickListener = object : ViewHolderClickListener {
        override fun onViewHolderClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType?) {
            onStreamClick(holder)
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

        setFragmentResultListener(REQUEST_FILTER_QUERY + tabType) { _, bundle ->
            filterStreams(bundle.getString(REQUEST_FILTER_QUERY_KEY, ""))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChannelsTabBinding.inflate(inflater, container, false)
        initUI()
        updateStreams()

        return binding.root
    }

    private fun updateStreams() {
        when (tabType) {
            ChannelsTabType.ALL -> adapter.items = channelsInteractor.getAllStreams()
            ChannelsTabType.SUBSCRIBED -> adapter.items = channelsInteractor.getSubscribedStreams()
        }
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

        updateStreams()
    }

    private fun filterStreams(query: String) {
        when (tabType) {
            ChannelsTabType.ALL -> channelsInteractor.filterAllStreams(query)
            ChannelsTabType.SUBSCRIBED -> channelsInteractor.filterSubscribedStreams(query)
        }

        updateStreams()
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