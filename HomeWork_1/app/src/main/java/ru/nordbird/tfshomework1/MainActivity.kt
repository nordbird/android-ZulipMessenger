package ru.nordbird.tfshomework1

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import ru.nordbird.tfshomework1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var contactAdapter: ContactAdapter

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.extras?.getStringArray("result").orEmpty()
                contactAdapter.updateData(data.toList())

                if (data.isEmpty())
                    Toast.makeText(this, "Empty result", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Main Activity"

        binding.btnOpenSecond.setOnClickListener {
            startForResult.launch(Intent(this, SecondActivity::class.java))
        }

        contactAdapter = ContactAdapter()
        val linearLayoutManager = LinearLayoutManager(this)

        with(binding.rvContacts) {
            layoutManager = linearLayoutManager
            adapter = contactAdapter
        }
    }


}