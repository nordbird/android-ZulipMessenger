package ru.nordbird.tfsmessenger.ui.topic

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.databinding.BottomSheetReactionBinding
import ru.nordbird.tfsmessenger.databinding.FragmentTopicBinding
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_COLOR
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_NAME
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_STREAM_ID
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_STREAM_NAME
import ru.nordbird.tfsmessenger.ui.custom.ReactionView
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.*

class TopicFragment : Fragment() {

    private var _binding: FragmentTopicBinding? = null
    private val binding get() = _binding!!

    private var streamId: String? = null
    private var streamName: String? = null
    private var topicName: String? = null
    private var topicColor: Int = 0

    private val topicInteractor = TopicInteractor
    private val compositeDisposable = CompositeDisposable()

    private var isTextMode = false
    private val currentUser = DataGenerator.getCurrentUser()

    private val clickListener: ViewHolderClickListener = object : ViewHolderClickListener {
        override fun onViewHolderClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType?) {
            when (holder.itemViewType) {
                R.layout.item_message_in, R.layout.item_message_out -> onMessageClick(holder, view, clickType)
                R.layout.item_error -> onReloadClick()
            }
        }

        override fun onViewHolderLongClick(holder: BaseViewHolder<*>, view: View): Boolean {
            return onMessageLongClick(holder)
        }
    }

    private val holderFactory = TfsHolderFactory(currentUser.id.toString(), clickListener)
    private val adapter = Adapter<ViewTyped>(holderFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            streamId = it.getString(REQUEST_OPEN_TOPIC_STREAM_ID)
            streamName = it.getString(REQUEST_OPEN_TOPIC_STREAM_NAME)
            topicName = it.getString(REQUEST_OPEN_TOPIC_NAME)
            topicColor = it.getInt(REQUEST_OPEN_TOPIC_COLOR)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTopicBinding.inflate(inflater, container, false)
        initUI()
        initToolbar()
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            activity?.onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
        topicInteractor.clearDisposable()
    }

    private fun initUI() {
        binding.tvTopicTitle.text = topicName
        val linearLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        binding.rvChat.adapter = adapter
        binding.rvChat.layoutManager = linearLayoutManager

        binding.ibSend.setOnClickListener {
            if (isTextMode) {
                topicInteractor.addMessage(currentUser, binding.edMessage.text.toString()).subscribe(
                    {
                        binding.edMessage.text.clear()
                        linearLayoutManager.scrollToPosition(0)
                    },
                    { err -> Toast.makeText(context, err.message, Toast.LENGTH_SHORT).show() }
                )
            }
        }

        binding.edMessage.doOnTextChanged { text, _, _, _ ->
            isTextMode = !text.isNullOrBlank()
            updateUI()
        }

        showShimmer()
        val usersDisposable = topicInteractor.getMessages()
            .subscribe { updateMessages(it) }
        compositeDisposable.add(usersDisposable)
    }

    private fun initToolbar() {
        with(activity as AppCompatActivity) {
            setSupportActionBar(binding.appbar.toolbar)
            supportActionBar?.title = streamName
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding.appbar.toolbar.setBackgroundColor(topicColor)
        activity?.window?.statusBarColor = topicColor
    }

    private fun updateUI() {
        if (isTextMode) {
            binding.ibSend.setImageResource(R.drawable.ic_baseline_send_24)
        } else {
            binding.ibSend.setImageResource(R.drawable.ic_baseline_add_24)
        }
    }

    private fun showShimmer() {
        adapter.items = listOf(TopicShimmerUi(), TopicShimmerUi())
    }

    private fun updateMessages(resource: List<ViewTyped>) {
        adapter.items = resource
    }

    private fun showReactionChooser(messageId: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetReactionBinding.inflate(layoutInflater)
        val sheetView = sheetBinding.root
        val table = sheetBinding.tlTable
        table.isStretchAllColumns = true
        table.isShrinkAllColumns = true

        var code = REACTION_FIRST_CODE
        repeat(REACTION_SHEET_ROWS) {
            val tableRow = TableRow(context)
            tableRow.gravity = Gravity.CENTER_HORIZONTAL
            repeat(REACTION_SHEET_COLS) {
                val reactionView = TextView(context, null, 0, R.style.BottomSheetReactionStyle)
                reactionView.text = getReaction(code)
                tableRow.addView(reactionView)
                val localCode = getReaction(code)
                reactionView.setOnClickListener {
                    topicInteractor.updateReaction(messageId, currentUser.id.toString(), localCode).subscribe(
                        { bottomSheetDialog.dismiss() },
                        { err -> Toast.makeText(context, err.message, Toast.LENGTH_SHORT).show() }
                    )
                }
                code++
            }

            table.addView(tableRow)
        }

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    private fun getReaction(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    private fun onMessageClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType?) {
        when (clickType) {
            MessageVHClickType.UPDATE_REACTION_CLICK -> {
                val reactionView = view as ReactionView
                topicInteractor.updateReaction(holder.itemId, currentUser.id.toString(), reactionView.reactionCode).subscribe(
                    {},
                    { err -> Toast.makeText(context, err.message, Toast.LENGTH_SHORT).show() }
                )
            }
            MessageVHClickType.ADD_REACTION_CLICK -> showReactionChooser(holder.itemId)
        }
    }

    private fun onMessageLongClick(holder: BaseViewHolder<*>): Boolean {
        showReactionChooser(holder.itemId)
        return true
    }

    private fun onReloadClick() {
        showShimmer()
        compositeDisposable.add(topicInteractor.loadMessages())
    }

    companion object {
        private const val REACTION_SHEET_ROWS = 5
        private const val REACTION_SHEET_COLS = 10
        private const val REACTION_FIRST_CODE = 0x1F600

    }
}