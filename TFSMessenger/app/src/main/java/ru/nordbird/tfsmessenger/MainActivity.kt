package ru.nordbird.tfsmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.children
import ru.nordbird.tfsmessenger.databinding.ActivityMainBinding
import ru.nordbird.tfsmessenger.databinding.MessageViewGroupBinding
import ru.nordbird.tfsmessenger.ui.custom.EmojiView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mvgBinding: MessageViewGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mvgBinding = MessageViewGroupBinding.bind(binding.mvgMain)
        setContentView(binding.root)

        mvgBinding.fblEmoji.children.filter { it is EmojiView }.forEach { child ->
            child.setOnClickListener {
                it.isSelected = !it.isSelected
            }
        }
    }
}