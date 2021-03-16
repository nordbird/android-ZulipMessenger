package ru.nordbird.tfsmessenger

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.nordbird.tfsmessenger.data.mapper.BaseMessageToMessageUi
import ru.nordbird.tfsmessenger.data.repository.MessageRepository
import ru.nordbird.tfsmessenger.databinding.ActivityMainBinding
import ru.nordbird.tfsmessenger.databinding.BottomSheetReactionBinding
import ru.nordbird.tfsmessenger.ui.custom.ReactionView
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.ClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageInClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageOutClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.holder.TfsHolderFactory


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val messageRepository = MessageRepository
    private var isTextMode = false

    private val clickListener = { viewTyped: ViewTyped, view: View, viewType: ClickedViewType ->
        when (viewTyped.viewType) {
            R.layout.item_message_in -> onMessageInClick(viewTyped, view, viewType)
            R.layout.item_message_out -> onMessageOutClick(viewTyped, view, viewType)
        }
    }

    private val longClickListener = { viewTyped: ViewTyped, view: View, viewType: ClickedViewType ->
        when (viewTyped.viewType) {
            R.layout.item_message_in -> onMessageInClick(viewTyped, view, viewType)
            R.layout.item_message_out -> onMessageOutClick(viewTyped, view, viewType)
        }
    }

    private val holderFactory = TfsHolderFactory(clickListener, longClickListener)
    private val adapter = Adapter<ViewTyped>(holderFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        updateUI()
        updateMessages()
    }

    private fun initUI() {
        binding.rvChat.adapter = adapter
        binding.rvChat.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, true)

        binding.ibSend.setOnClickListener {
            if (isTextMode) {
                messageRepository.addMessage(binding.edMessage.text.toString())
                binding.edMessage.text.clear()
                updateMessages()
            }
        }

        binding.edMessage.doOnTextChanged { text, start, before, count ->
            isTextMode = binding.edMessage.text.isNotEmpty()
            updateUI()
        }
    }

    private fun updateUI() {
        if (isTextMode) {
            binding.ibSend.setImageResource(R.drawable.ic_baseline_send_24)
        } else {
            binding.ibSend.setImageResource(R.drawable.ic_baseline_add_24)
        }
    }

    private fun updateMessages() {
        adapter.items = BaseMessageToMessageUi()(messageRepository.getMessages())
    }

    private fun showReactionChooser(viewTyped: ViewTyped) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetReactionBinding.inflate(layoutInflater)
        val sheetView = sheetBinding.root
        val table = sheetBinding.tlTable
        table.isStretchAllColumns = true
        table.isShrinkAllColumns = true

        var code = 0x1F600
        (1..REACTION_SHEET_ROWS).forEach {
            val tableRow = TableRow(this)
            tableRow.gravity = Gravity.CENTER_HORIZONTAL
            (1..REACTION_SHEET_COLS).forEach {
                val reactionView = TextView(this, null, 0, R.style.BottomSheetReactionStyle)
                reactionView.text = getReaction(code)
                tableRow.addView(reactionView)
                val localCode = code
                reactionView.setOnClickListener {
                    messageRepository.addReaction(viewTyped.uid, localCode)
                    bottomSheetDialog.dismiss()
                    Toast.makeText(this, getReaction(localCode), Toast.LENGTH_SHORT).show()
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

    private fun onMessageInClick(viewTyped: ViewTyped, view: View, viewType: ClickedViewType) {
        when (viewType) {
            MessageInClickedViewType.REACTION -> {
                val reactionView = view as ReactionView
                reactionView.isSelected = !reactionView.isSelected
                reactionView.reactionCount += if (reactionView.isSelected) 1 else -1
            }
            MessageInClickedViewType.ADD_REACTION -> showReactionChooser(viewTyped)
            MessageInClickedViewType.LONG_MESSAGE -> showReactionChooser(viewTyped)
        }
    }

    private fun onMessageOutClick(viewTyped: ViewTyped, view: View, viewType: ClickedViewType) {
        when (viewType) {
            MessageOutClickedViewType.REACTION -> {
                val reactionView = view as ReactionView
                reactionView.isSelected = !reactionView.isSelected
                reactionView.reactionCount += if (reactionView.isSelected) 1 else -1
            }
            MessageOutClickedViewType.ADD_REACTION -> showReactionChooser(viewTyped)
            MessageOutClickedViewType.LONG_MESSAGE -> showReactionChooser(viewTyped)
        }
    }

    companion object {
        private const val REACTION_SHEET_ROWS = 5
        private const val REACTION_SHEET_COLS = 10
    }
}