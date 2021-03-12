package ru.nordbird.tfsmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.nordbird.tfsmessenger.databinding.ActivityMainBinding
import ru.nordbird.tfsmessenger.databinding.EmojiViewBinding
import ru.nordbird.tfsmessenger.databinding.MessageViewGroupBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mvgBinding: MessageViewGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mvgBinding = MessageViewGroupBinding.bind(binding.mvgMain)
        setContentView(binding.root)

        mvgBinding.fblEmoji.getChilds().forEach { child ->
            child.setOnClickListener {
                it.isSelected = !it.isSelected
            }
        }

        mvgBinding.fblEmoji.btnAddView.setOnClickListener {
            EmojiViewBinding.inflate(layoutInflater, mvgBinding.fblEmoji, true).root.apply {
                emojiCount = (20..29).random()
                setOnClickListener { emojiView ->
                    emojiView.isSelected = !emojiView.isSelected
                    this.emojiCount += if (emojiView.isSelected) 1 else -1
                    mvgBinding.fblEmoji.removeView(emojiView)
                }
            }
        }

    }
}