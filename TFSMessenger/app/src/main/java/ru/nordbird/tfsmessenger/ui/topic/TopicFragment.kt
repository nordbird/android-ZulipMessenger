package ru.nordbird.tfsmessenger.ui.topic

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.App
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.data.emojiSet.EMOJI_SET
import ru.nordbird.tfsmessenger.data.model.TopicColorType
import ru.nordbird.tfsmessenger.databinding.BottomSheetReactionBinding
import ru.nordbird.tfsmessenger.databinding.FragmentTopicBinding
import ru.nordbird.tfsmessenger.extensions.userMessage
import ru.nordbird.tfsmessenger.ui.custom.ReactionView
import ru.nordbird.tfsmessenger.ui.main.MainActivity
import ru.nordbird.tfsmessenger.ui.mvi.base.MviFragment
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.*
import ru.nordbird.tfsmessenger.ui.recycler.holder.*
import ru.nordbird.tfsmessenger.ui.topic.base.TopicAction
import ru.nordbird.tfsmessenger.ui.topic.base.TopicPresenter
import ru.nordbird.tfsmessenger.ui.topic.base.TopicUiEffect
import ru.nordbird.tfsmessenger.ui.topic.base.TopicView
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject

class TopicFragment : MviFragment<TopicView, TopicAction, TopicPresenter>(), TopicView {

    private var _binding: FragmentTopicBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityListener: TopicFragmentListener

    @Inject
    lateinit var topicPresenter: TopicPresenter

    private val compositeDisposable = CompositeDisposable()

    private var streamName: String = ""
    private var topicName: String = ""
    private var colorType: TopicColorType = TopicColorType.TOPIC1

    private var isTextMode = false
    private var needScroll: Boolean = false
    private val currentUserId = ZulipAuth.AUTH_ID

    private val clickListener: ViewHolderClickListener = object : ViewHolderClickListener {
        override fun onViewHolderClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType?) {
            when (holder.itemViewType) {
                R.layout.item_separator_topic -> onTopicClick(holder)
                R.layout.item_message_in, R.layout.item_message_out -> onMessageClick(holder, view, clickType)
                R.layout.item_attachment_in, R.layout.item_attachment_out -> onAttachmentClick(holder)
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

    override fun getPresenter(): TopicPresenter = topicPresenter

    override fun getMviView(): TopicView = this

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TopicFragmentListener) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement TopicFragmentListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        var colorTypeName = ""
        arguments?.let {
            streamName = it.getString(OPEN_TOPIC_STREAM_NAME, "")
            topicName = it.getString(OPEN_TOPIC_NAME, "")
            colorTypeName = it.getString(OPEN_TOPIC_COLOR_TYPE_NAME, "")

        }
        if (colorTypeName.isNotEmpty()) colorType = TopicColorType.valueOf(colorTypeName)

        topicPresenter = if (isSingleTopic()) {
            App.instance.provideTopicComponent().provideTopicPresenter()
        } else {
            App.instance.provideTopicComponent().provideStreamPresenter()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireContext().theme.applyStyle(colorType.style, true)
        _binding = FragmentTopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPresenter().input.accept(TopicAction.RegisterEventQueue(streamName, topicName))

        initUI()
        initToolbar()
        loadMessages()
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
        compositeDisposable.clear()
        getPresenter().input.accept(TopicAction.DeleteEventQueue)
        super.onDestroyView()
    }

    private fun initUI() {
        if (isSingleTopic()) {
            binding.tvTopicTitle.text = getString(R.string.topic_name, topicName)
        } else {
            binding.tvTopicTitle.visibility = View.GONE
        }
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
            if (needScroll) {
                binding.rvChat.layoutManager?.scrollToPosition(0)
            }
        }
        compositeDisposable.add(disposable)
    }

    override fun render(state: TopicState) {
        needScroll = state.needScroll
        adapter.items = state.items
    }

    override fun handleUiEffect(uiEffect: TopicUiEffect) {
        when (uiEffect) {
            is TopicUiEffect.DownloadFile -> saveFile(uiEffect.stream)
            is TopicUiEffect.ActionError -> showError(uiEffect.error)
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
        if (isSingleTopic()) {
            val color = ContextCompat.getColor(requireContext(), colorType.color)
            binding.appbar.toolbar.setBackgroundColor(color)
            requireActivity().window.statusBarColor = color
        }
    }

    private fun updateUI() {
        val icon = if (isTextMode) R.drawable.ic_baseline_send_24 else R.drawable.ic_baseline_add_24
        binding.ibSend.setImageResource(icon)
    }

    private fun showError(throwable: Throwable) {
        val view = (requireActivity() as MainActivity).rootView
        Snackbar.make(view, throwable.userMessage(requireContext()), Snackbar.LENGTH_SHORT).show()
    }

    private fun loadMessages() {
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
        }
    }

    private fun onMessageLongClick(holder: BaseViewHolder<*>): Boolean {
        val message = adapter.items[holder.absoluteAdapterPosition] as MessageUi
        showReactionChooser(message)
        return true
    }

    private fun onReloadClick() {
        loadMessages()
    }

    private fun onAttachmentClick(holder: BaseViewHolder<*>) {
        val attachment = adapter.items[holder.absoluteAdapterPosition] as AttachmentUi
        getPresenter().input.accept(TopicAction.DownloadFile(attachment.url))
    }

    private fun onTopicClick(holder: BaseViewHolder<*>) {
        val topicSeparator = adapter.items[holder.absoluteAdapterPosition] as SeparatorTopicUi
        activityListener.onOpenTopic(
            bundleOf(
                OPEN_TOPIC_STREAM_NAME to streamName,
                OPEN_TOPIC_NAME to topicSeparator.name,
                OPEN_TOPIC_COLOR_TYPE_NAME to topicSeparator.colorType.name
            )
        )
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

    private fun isSingleTopic(): Boolean = topicName.isNotEmpty()

    interface TopicFragmentListener {

        fun onOpenTopic(bundle: Bundle)

    }

    companion object {
        private const val REACTION_SHEET_ROWS = 10
        private const val REACTION_SHEET_COLS = 10
        private const val ITEM_THRESHOLD = 5
        private const val DEFAULT_FILE_NAME = "NoName"

        const val OPEN_TOPIC_STREAM_NAME = "stream_name"
        const val OPEN_TOPIC_NAME = "topic_name"
        const val OPEN_TOPIC_COLOR_TYPE_NAME = "topic_color_type_name"
    }
}