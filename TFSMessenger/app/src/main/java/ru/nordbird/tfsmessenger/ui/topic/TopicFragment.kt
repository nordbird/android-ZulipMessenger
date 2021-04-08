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
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.data.emojiSet.EMOJI_SET
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

    private var streamId: String = ""
    private var streamName: String = ""
    private var topicName: String = ""
    private var topicColor: Int = 0

    private val topicInteractor = TopicInteractor
    private val compositeDisposable = CompositeDisposable()

    private var isTextMode = false
    private val currentUserId = ZulipAuth.AUTH_ID

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

    private val holderFactory = TfsHolderFactory(currentUserId, clickListener)
    private val adapter = Adapter<ViewTyped>(holderFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            streamId = it.getString(REQUEST_OPEN_TOPIC_STREAM_ID, "")
            streamName = it.getString(REQUEST_OPEN_TOPIC_STREAM_NAME, "")
            topicName = it.getString(REQUEST_OPEN_TOPIC_NAME, "")
            topicColor = it.getInt(REQUEST_OPEN_TOPIC_COLOR)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTopicBinding.inflate(inflater, container, false)
        initUI()
        initToolbar()
        updateMessages(true, true)
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
    }

    private fun initUI() {
        binding.tvTopicTitle.text = topicName
        val linearLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        binding.rvChat.adapter = adapter
        binding.rvChat.layoutManager = linearLayoutManager

        binding.ibSend.setOnClickListener {
            if (isTextMode) {
                addMessage()
            }
        }

        binding.edMessage.doOnTextChanged { text, _, _, _ ->
            isTextMode = !text.isNullOrBlank()
            updateUI()
        }

    }

    private fun addMessage() {
        val disposable = topicInteractor.addMessage(streamName, topicName, binding.edMessage.text.toString())
            .subscribe(
                { response ->
                    if (response.result == "success") {
                        binding.edMessage.text.clear()
                        updateMessages(false, true)
                    } else {
                        Toast.makeText(context, response.msg, Toast.LENGTH_SHORT).show()
                    }
                },
                { err -> Toast.makeText(context, err.message, Toast.LENGTH_SHORT).show() }
            )
        compositeDisposable.add(disposable)
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

    private fun showError(throwable: Throwable) {
        adapter.items = listOf(ErrorUi())
        Snackbar.make(binding.root, throwable.message.toString(), Snackbar.LENGTH_SHORT).show()
    }

    private fun updateMessages(needShimmer: Boolean, needScroll: Boolean) {
        if (needShimmer) showShimmer()

        val usersDisposable = topicInteractor.getMessages(streamName, topicName)
            .subscribe(
                {
                    adapter.items = it
                    if (needScroll) binding.rvChat.layoutManager?.scrollToPosition(0)
                },
                { showError(it) }
            )
        compositeDisposable.add(usersDisposable)
    }

    private fun showReactionChooser(message: MessageUi) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetReactionBinding.inflate(layoutInflater)
        val sheetView = sheetBinding.root
        val table = sheetBinding.tlTable
        table.isStretchAllColumns = true
        table.isShrinkAllColumns = true

        repeat(REACTION_SHEET_ROWS) { row ->
            val tableRow = TableRow(context)
            tableRow.gravity = Gravity.CENTER_HORIZONTAL
            repeat(REACTION_SHEET_COLS) { col ->
                val reactionView = TextView(context, null, 0, R.style.BottomSheetReactionStyle)
                val code = EMOJI_SET[row * REACTION_SHEET_COLS + col].getCodeString()
                reactionView.text = code
                tableRow.addView(reactionView)
                reactionView.setOnClickListener {
                    updateReaction(message, code)
                    bottomSheetDialog.dismiss()
                }
            }
            table.addView(tableRow)
        }

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    private fun updateReaction(message: MessageUi, reactionCode: String) {
        val disposable = topicInteractor.updateReaction(message, currentUserId, reactionCode).subscribe(
            { response ->
                if (response.result == "success") {
                    updateMessages(false, false)
                } else {
                    Toast.makeText(context, response.msg, Toast.LENGTH_SHORT).show()
                }

            },
            { err -> Toast.makeText(context, err.message, Toast.LENGTH_SHORT).show() }
        )
        compositeDisposable.add(disposable)
    }

    private fun onMessageClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType?) {
        val message = adapter.items[holder.absoluteAdapterPosition] as MessageUi
        when (clickType) {
            MessageVHClickType.UPDATE_REACTION_CLICK -> {
                val reactionView = view as ReactionView

                updateReaction(message, reactionView.reactionCode)
            }
            MessageVHClickType.ADD_REACTION_CLICK -> showReactionChooser(message)
        }
    }

    private fun onMessageLongClick(holder: BaseViewHolder<*>): Boolean {
        val message = adapter.items[holder.absoluteAdapterPosition] as MessageUi
        showReactionChooser(message)
        return true
    }

    private fun onReloadClick() {
        updateMessages(true, true)
    }

    companion object {
        private const val REACTION_SHEET_ROWS = 5
        private const val REACTION_SHEET_COLS = 10
    }
}