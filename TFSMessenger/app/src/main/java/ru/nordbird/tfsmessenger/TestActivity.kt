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
import ru.nordbird.tfsmessenger.data.mapper.MessageToViewTypedMapper
import ru.nordbird.tfsmessenger.data.repository.MessageRepository
import ru.nordbird.tfsmessenger.databinding.ActivityTestBinding
import ru.nordbird.tfsmessenger.databinding.BottomSheetReactionBinding
import ru.nordbird.tfsmessenger.ui.custom.ReactionView
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageVHClickType
import ru.nordbird.tfsmessenger.ui.recycler.holder.TfsHolderFactory


class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding

    private val messageRepository = MessageRepository
    private val messageMapper = MessageToViewTypedMapper()
    private var isTextMode = false
    private val currentUser = DataGenerator.getCurrentUser()

    private val clickListener: ViewHolderClickListener = object : ViewHolderClickListener {
        override fun onViewHolderClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType) {
            onMessageClick(holder, view, clickType)
        }

        override fun onViewHolderLongClick(holder: BaseViewHolder<*>, view: View): Boolean {
            return onMessageLongClick(holder)
        }
    }

    private val holderFactory = TfsHolderFactory(currentUser, clickListener)
    private val adapter = Adapter<ViewTyped>(holderFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
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
            isTextMode = !text.isNullOrBlank()
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
        adapter.items = messageMapper.transform(messageRepository.getMessages())
    }

    private fun showReactionChooser(messageId: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetReactionBinding.inflate(layoutInflater)
        val sheetView = sheetBinding.root
        val table = sheetBinding.tlTable
        table.isStretchAllColumns = true
        table.isShrinkAllColumns = true

        var code = REACTION_FIRST_CODE
        repeat(REACTION_SHEET_ROWS) {
            val tableRow = TableRow(this)
            tableRow.gravity = Gravity.CENTER_HORIZONTAL
            repeat(REACTION_SHEET_COLS) {
                val reactionView = TextView(this, null, 0, R.style.BottomSheetReactionStyle)
                reactionView.text = getReaction(code)
                tableRow.addView(reactionView)
                val localCode = code
                reactionView.setOnClickListener {
                    messageRepository.updateReaction(messageId, currentUser.id, localCode)
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

    private fun onMessageClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType) {
        when (clickType) {
            MessageVHClickType.UPDATE_REACTION_CLICK -> {
                val reactionView = view as ReactionView
                messageRepository.updateReaction(holder.itemId, currentUser.id, reactionView.reactionCode)
                updateMessages()
            }
            MessageVHClickType.ADD_REACTION_CLICK -> showReactionChooser(holder.itemId)
        }
    }

    private fun onMessageLongClick(holder: BaseViewHolder<*>): Boolean {
        showReactionChooser(holder.itemId)
        return true
    }

    companion object {
        private const val REACTION_SHEET_ROWS = 5
        private const val REACTION_SHEET_COLS = 10
        private const val REACTION_FIRST_CODE = 0x1F600
    }
}