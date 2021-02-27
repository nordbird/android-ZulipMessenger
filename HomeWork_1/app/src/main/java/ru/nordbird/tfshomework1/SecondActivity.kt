package ru.nordbird.tfshomework1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        title = "Second Activity"
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent()
        intent.putExtra("result", "OK!")
        setResult(RESULT_OK, intent)
        finish()
    }
}