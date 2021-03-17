package ru.nordbird.tfsmessenger

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.data.mapper.BaseMessageToMessageUi
import ru.nordbird.tfsmessenger.data.repository.MessageRepository
import ru.nordbird.tfsmessenger.databinding.ActivityMainBinding
import ru.nordbird.tfsmessenger.databinding.BottomSheetReactionBinding
import ru.nordbird.tfsmessenger.ui.custom.ReactionView
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.ClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.holder.TfsHolderFactory


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val messageRepository = MessageRepository
    private var isTextMode = false
    private val currentUser = DataGenerator.getCurrentUser()

    private val clickListener = { viewTyped: ViewTyped, view: View, viewType: ClickedViewType ->
        when (viewTyped.viewType) {
            R.layout.item_message_in -> onMessageClick(viewTyped, view, viewType)
            R.layout.item_message_out -> onMessageClick(viewTyped, view, viewType)
        }
    }

    private val holderFactory = TfsHolderFactory(currentUser, clickListener, clickListener)
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
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, true)
        binding.rvChat.adapter = adapter
        binding.rvChat.layoutManager = linearLayoutManager

        binding.ibSend.setOnClickListener {
            if (isTextMode) {
                messageRepository.addMessage(currentUser, binding.edMessage.text.toString())
                binding.edMessage.text.clear()
                updateMessages()
                linearLayoutManager.scrollToPosition(0)
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
                    messageRepository.updateReaction(viewTyped.uid, currentUser.id, localCode)
                    bottomSheetDialog.dismiss()
                    updateMessages()
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

    private fun onMessageClick(viewTyped: ViewTyped, view: View, viewType: ClickedViewType) {
        when (viewType) {
            MessageClickedViewType.REACTION -> {
                val reactionView = view as ReactionView
                messageRepository.updateReaction(viewTyped.uid, currentUser.id, reactionView.reactionCode)
                updateMessages()
            }
            MessageClickedViewType.ADD_REACTION -> showReactionChooser(viewTyped)
            MessageClickedViewType.LONG_MESSAGE -> showReactionChooser(viewTyped)
        }
    }

    companion object {
        private const val REACTION_SHEET_ROWS = 5
        private const val REACTION_SHEET_COLS = 10
    }
}