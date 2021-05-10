package ru.nordbird.tfsmessenger.ui.topic

import android.content.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ru.nordbird.tfsmessenger.App
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentEditMessageBinding
import ru.nordbird.tfsmessenger.extensions.userMessage
import ru.nordbird.tfsmessenger.ui.main.MainActivity
import ru.nordbird.tfsmessenger.ui.mvi.base.MviFragment
import ru.nordbird.tfsmessenger.ui.topic.base.*
import javax.inject.Inject
import javax.inject.Named

class EditMessageFragment : MviFragment<TopicView, TopicAction, TopicPresenter>(), TopicView {

    private var _binding: FragmentEditMessageBinding? = null
    private val binding get() = _binding!!

    @Inject
    @Named(EDIT_MESSAGE_PRESENTER)
    lateinit var topicPresenter: TopicPresenter

    private var streamId: Int = 0
    private var streamName: String = ""
    private var topicName: String = ""
    private var messageId: Int = 0

    private lateinit var topicsAdapter: ArrayAdapter<String>

    override fun getPresenter(): TopicPresenter = topicPresenter

    override fun getMviView(): TopicView = this

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.instance.provideTopicComponent().inject(this)
        topicsAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            messageId = it.getInt(EDIT_MESSAGE_ID)
            streamId = it.getInt(EDIT_MESSAGE_STREAM_ID)
            streamName = it.getString(EDIT_MESSAGE_STREAM_NAME, "")
            topicName = it.getString(EDIT_MESSAGE_TOPIC_NAME, "")

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initToolbar()
        loadMessage()
        loadTopics()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            activity?.onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun initUI() {
        binding.tvMessageTopic.setText(topicName)
        binding.btnUpdate.setOnClickListener {
            updateMessage()
        }

        binding.tvMessageTopic.setAdapter(topicsAdapter)
    }

    override fun render(state: TopicState) {}

    override fun handleUiEffect(uiEffect: TopicUiEffect) {
        when (uiEffect) {
            is TopicUiEffect.ActionError -> {
                showError(uiEffect.error)
                binding.btnUpdate.isEnabled = true
            }
            is TopicUiEffect.TopicsLoaded -> updateTopics(uiEffect.topics)
            is TopicUiEffect.MessageLoaded -> updateContent(uiEffect.content)
            TopicUiEffect.MessageUpdated -> activity?.onBackPressed()
        }
    }

    private fun updateContent(content: String) {
        binding.etMessageContent.setText(content)
    }

    private fun updateMessage() {
        binding.btnUpdate.isEnabled = false
        val topic = if (binding.tvMessageTopic.text.isNotBlank()) {
            binding.tvMessageTopic.text.toString()
        } else {
            getString(R.string.default_topic_name)
        }
        val content = binding.etMessageContent.text.toString()
        getPresenter().input.accept(TopicAction.UpdateMessage(messageId, topic, content))
    }

    private fun initToolbar() {
        with(activity as AppCompatActivity) {
            setSupportActionBar(binding.appbar.toolbar)
            supportActionBar?.title = getString(R.string.action_edit_message)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun showError(throwable: Throwable) {
        val view = (requireActivity() as MainActivity).rootView
        Snackbar.make(view, throwable.userMessage(requireContext()), Snackbar.LENGTH_SHORT).show()
    }

    private fun loadMessage() {
        getPresenter().input.accept(TopicAction.LoadMessage(messageId))
    }

    private fun loadTopics() {
        getPresenter().input.accept(TopicAction.LoadTopics(streamId, streamName))
    }

    private fun updateTopics(topics: List<String>) {
        topicsAdapter.clear()
        topicsAdapter.addAll(topics)
        topicsAdapter.notifyDataSetChanged()
    }

    companion object {
        const val EDIT_MESSAGE_ID = "message_id"
        const val EDIT_MESSAGE_STREAM_ID = "stream_id"
        const val EDIT_MESSAGE_STREAM_NAME = "stream_name"
        const val EDIT_MESSAGE_TOPIC_NAME = "topic_name"
    }
}