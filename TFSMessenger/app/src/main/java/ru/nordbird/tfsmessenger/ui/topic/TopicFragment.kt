package ru.nordbird.tfsmessenger.ui.topic

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.*
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import ru.nordbird.tfsmessenger.di.GlobalDI
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_COLOR
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_NAME
import ru.nordbird.tfsmessenger.ui.channels.ChannelsFragment.Companion.REQUEST_OPEN_TOPIC_STREAM_NAME
import ru.nordbird.tfsmessenger.ui.custom.ReactionView
import ru.nordbird.tfsmessenger.ui.mvi.base.MviFragment
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.*
import ru.nordbird.tfsmessenger.ui.recycler.holder.*
import ru.nordbird.tfsmessenger.utils.network.RxConnectionObservable
import java.io.FileNotFoundException
import java.io.InputStream


class TopicFragment : MviFragment<TopicView, TopicPresenter>(), TopicView {

    private var _binding: FragmentTopicBinding? = null
    private val binding get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    private var streamName: String = ""
    private var topicName: String = ""
    private var topicColor: Int = 0

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

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            showFileChooser()
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            sendFile(result.data?.data)
        }
    }

    private val holderFactory = TfsHolderFactory(currentUserId, clickListener)
    private val diffUtilCallback = DiffUtilCallback<ViewTyped>()
    private val adapter = Adapter(holderFactory, diffUtilCallback)

    private var lastState: TopicState = TopicState()

    override fun getPresenter(): TopicPresenter = GlobalDI.INSTANCE.topicPresenter

    override fun getMviView(): TopicView = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            streamName = it.getString(REQUEST_OPEN_TOPIC_STREAM_NAME, "")
            topicName = it.getString(REQUEST_OPEN_TOPIC_NAME, "")
            topicColor = it.getInt(REQUEST_OPEN_TOPIC_COLOR)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTopicBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initToolbar()
        updateMessages()
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

        val disposable = adapter.updateAction.subscribe {
            if (lastState.needScroll) {
                binding.rvChat.layoutManager?.scrollToPosition(0)
            }
        }
        compositeDisposable.add(disposable)
    }

    override fun render(state: TopicState) {
        lastState = state
        adapter.items = state.items
    }

    override fun handleUiEffect(uiEffect: TopicUiEffect) {
        when (uiEffect) {
            is TopicUiEffect.DownloadFile -> saveFile(uiEffect.stream)
            is TopicUiEffect.LoadMessagesError -> showError(uiEffect.error)
        }
    }

    private fun addMessage(content: String) {
        getPresenter().input.accept(TopicAction.SendMessage(streamName, topicName, content))
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

    private fun showError(throwable: Throwable) {
        Snackbar.make(binding.root, throwable.message.toString(), Snackbar.LENGTH_SHORT).show()
    }

    private fun updateMessages() {
        getPresenter().input.accept(TopicAction.FirstLoadMessages(streamName, topicName))
    }

    private fun moreMessages() {
        getPresenter().input.accept(TopicAction.NextLoadMessages(streamName, topicName))
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
        getPresenter().input.accept(TopicAction.UpdateReaction(message, currentUserId, reactionCode))
    }

    private fun onMessageClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType?) {
        val message = adapter.items[holder.absoluteAdapterPosition] as MessageUi
        when (clickType) {
            MessageVHClickType.UPDATE_REACTION_CLICK -> {
                val reactionView = view as ReactionView

                updateReaction(message, reactionView.reactionCode)
            }
            MessageVHClickType.ADD_REACTION_CLICK -> showReactionChooser(message)
            MessageVHClickType.GET_ATTACHMENT_CLICK -> downloadAttachment(message)
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
                if (totalItemCount <= lastVisibleItemPosition + ITEM_THRESHOLD) {
                    moreMessages()
                }
            }
        }
        binding.rvChat.addOnScrollListener(scrollListener)
    }

    private fun sendFile(data: Uri?) {
        if (data == null) return
        val contentResolver = requireActivity().contentResolver
        try {
            val stream = contentResolver.openInputStream(data)
            getPresenter().input.accept(TopicAction.SendFile(streamName, topicName, getFileName(data), stream))
        } catch (ex: FileNotFoundException) {
            Snackbar.make(binding.root, getString(R.string.error_file_not_found), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun downloadAttachment(message: MessageUi) {
        getPresenter().input.accept(TopicAction.DownloadFile(message.link))
    }

    private fun saveFile(it: InputStream) {
        // тут должно быть сохранение и открытие файла
        Snackbar.make(binding.root, getString(R.string.info_file_downloaded), Snackbar.LENGTH_SHORT).show()
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

    private fun checkMediaPermission() {
        val permissionStatus = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            showFileChooser()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun getFileName(uri: Uri): String {
        var result = uri.lastPathSegment ?: DEFAULT_FILE_NAME
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
        private const val DEFAULT_FILE_NAME = "NoName"
    }
}