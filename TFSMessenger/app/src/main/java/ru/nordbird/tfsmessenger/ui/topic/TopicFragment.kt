package ru.nordbird.tfsmessenger.ui.topic

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.*
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
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

    private val topicInteractor = TopicInteractor()
    private val compositeDisposable = CompositeDisposable()

    private var loading = false
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
        updateMessages()
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
        setRecyclerViewScrollListener(linearLayoutManager)

        binding.ibSend.setOnClickListener {
            if (isTextMode) {
                addMessage(binding.edMessage.text.toString())
                binding.edMessage.text.clear()
            } else {
                checkMediaPermission()
            }
        }

        binding.edMessage.doOnTextChanged { text, _, _, _ ->
            isTextMode = !text.isNullOrBlank()
            updateUI()
        }

    }

    private fun addMessage(content: String) {
        val disposable = topicInteractor.addMessage(streamName, topicName, content)
            .subscribe(
                {
                    adapter.items = it
                    binding.rvChat.layoutManager?.scrollToPosition(0)
                },
                { showError(it) }
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

    private fun updateMessages() {
        showShimmer()

        val messagesDisposable = topicInteractor.getMessages(streamName, topicName)
            .subscribe(
                {
                    adapter.items = it
                    binding.rvChat.layoutManager?.scrollToPosition(0)
                },
                { showError(it) }
            )
        compositeDisposable.add(messagesDisposable)
    }

    private fun moreMessages() {
        val messagesDisposable = topicInteractor.getMessages(streamName, topicName)
            .subscribe(
                {
                    adapter.items = it
                    loading = false
                },
                { showError(it) }
            )
        compositeDisposable.add(messagesDisposable)
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
        val disposable = topicInteractor.updateReaction(message, currentUserId, reactionCode)
            .subscribe(
                { adapter.items = it },
                { showError(it) }
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
        updateMessages()
    }

    private fun setRecyclerViewScrollListener(linearLayoutManager: LinearLayoutManager) {
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = linearLayoutManager.itemCount
                val lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()
                if (totalItemCount <= lastVisibleItemPosition + ITEM_THRESHOLD && !loading) {
                    loading = true
                    moreMessages()
                }
            }
        }
        binding.rvChat.addOnScrollListener(scrollListener)
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startForResult.launch(Intent.createChooser(intent, getString(R.string.title_select_file)))
        } catch (ex: ActivityNotFoundException) {
            Snackbar.make(binding.root, getString(R.string.error_select_file), Snackbar.LENGTH_SHORT).show()
        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.data
                if (data != null) {
                    val contentResolver = requireActivity().contentResolver
                    val stream = contentResolver.openInputStream(data)

                    val disposable = topicInteractor.sendFile(streamName, topicName, getFileName(data), stream)
                        .subscribe(
                            { adapter.items = it },
                            { showError(it) }
                        )
                    compositeDisposable.add(disposable)
                }
            }
        }

    private val requestPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showFileChooser()
        }
    }

    private fun checkMediaPermission() {
        val permissionStatus =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            showFileChooser()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun getFileName(uri: Uri): String {
        var result = ""
        requireActivity().contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return result
    }

    companion object {
        private const val REACTION_SHEET_ROWS = 10
        private const val REACTION_SHEET_COLS = 10
        private const val ITEM_THRESHOLD = 5
    }
}