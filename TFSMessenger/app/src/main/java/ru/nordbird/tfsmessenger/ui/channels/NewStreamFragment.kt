package ru.nordbird.tfsmessenger.ui.channels

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.material.snackbar.Snackbar
import ru.nordbird.tfsmessenger.App
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentNewStreamBinding
import ru.nordbird.tfsmessenger.ui.channels.base.*
import ru.nordbird.tfsmessenger.ui.mvi.base.MviFragment
import javax.inject.Inject
import javax.inject.Named

class NewStreamFragment : MviFragment<ChannelsView, ChannelsAction, ChannelsPresenter>(), ChannelsView {

    private var _binding: FragmentNewStreamBinding? = null
    private val binding get() = _binding!!

    @Inject
    @Named(NEW_STREAM_CHANNELS_PRESENTER)
    lateinit var streamsChannelsPresenter: ChannelsPresenter

    private var lastState: ChannelsState = ChannelsState()

    override fun getPresenter(): ChannelsPresenter = streamsChannelsPresenter

    override fun getMviView(): ChannelsView = this

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.instance.provideChannelsComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewStreamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initToolbar()
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
        binding.btnCreate.setOnClickListener { createStream() }
        binding.etStreamName.doOnTextChanged { text, _, _, _ ->
            updateUI(text.toString())
        }
        getPresenter().input.accept(ChannelsAction.LoadStreams)
    }

    private fun updateUI(text: String) {
        val streamExists = lastState.streams.firstOrNull { it.name.equals(text.trim(), true) } != null
        binding.tiStreamName.error = if (streamExists) getString(R.string.error_stream_exists) else null
        binding.btnCreate.isEnabled = text.isNotBlank() && !streamExists
    }

    private fun initToolbar() {
        with(activity as AppCompatActivity) {
            setSupportActionBar(binding.appbar.toolbar)
            supportActionBar?.title = getString(R.string.title_stream_new)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun render(state: ChannelsState) {
        lastState = state
        updateUI(binding.etStreamName.text.toString())
    }

    override fun handleUiEffect(uiEffect: ChannelsUiEffect) {
        when (uiEffect) {
            is ChannelsUiEffect.ActionError -> {
                showError(uiEffect.error)
                binding.btnCreate.isEnabled = true
            }
            ChannelsUiEffect.StreamCreated -> activity?.onBackPressed()
        }
    }

    private fun showError(throwable: Throwable) {
        Snackbar.make(binding.root, throwable.message.toString(), Snackbar.LENGTH_SHORT).show()
    }

    private fun createStream() {
        binding.btnCreate.isEnabled = false
        getPresenter().input.accept(ChannelsAction.CreateStream(binding.etStreamName.text.toString().trim()))
    }

}