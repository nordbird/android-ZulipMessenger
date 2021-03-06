package ru.nordbird.tfsmessenger

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.nordbird.tfsmessenger.databinding.ActivityMainBinding
import ru.nordbird.tfsmessenger.extensions.spToPx

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.evSample.emojiCount = 8
        binding.evSample.emojiCode = 0x1F44D
        binding.evSample.textSize = spToPx(20F)
        binding.evSample.textColor = Color.GREEN
        binding.evSample.isSelected = true

        binding.evSample.setOnClickListener {
            it.isSelected = !it.isSelected
        }
    }
}