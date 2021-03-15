package ru.nordbird.tfsmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.nordbird.tfsmessenger.databinding.ActivityMainBinding
import ru.nordbird.tfsmessenger.databinding.LeftReactionViewBinding
import ru.nordbird.tfsmessenger.databinding.MessageInViewGroupBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mvgBinding: MessageInViewGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mvgBinding = MessageInViewGroupBinding.bind(binding.mvgMain)
        setContentView(binding.root)

        createReactionView()

        mvgBinding.fblReaction.getChilds().forEach { child ->
            child.setOnClickListener {
                it.isSelected = !it.isSelected
            }
        }

        mvgBinding.fblReaction.btnAddView.setOnClickListener {
            createReactionView()
        }

    }

    private fun createReactionView() {
        LeftReactionViewBinding.inflate(layoutInflater, mvgBinding.fblReaction, true).root.apply {
            reactionCount = (20..29).random()
            setOnClickListener { reactionView ->
                reactionView.isSelected = !reactionView.isSelected
                this.reactionCount += if (reactionView.isSelected) 1 else -1
            }
        }
    }
}