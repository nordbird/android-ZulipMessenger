package ru.nordbird.tfshomework1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.nordbird.tfshomework1.service.ContactIntentService

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        title = "Second Activity"
    }

    override fun onResume() {
        super.onResume()

        startService(Intent(this, ContactIntentService::class.java))

        val intent = Intent()
        intent.putExtra("result", "OK!")
        setResult(RESULT_OK, intent)
        finish()
    }
}