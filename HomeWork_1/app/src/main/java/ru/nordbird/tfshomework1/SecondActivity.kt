package ru.nordbird.tfshomework1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ru.nordbird.tfshomework1.service.ContactIntentService


class SecondActivity : AppCompatActivity() {

    private val contactReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            sendResult(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        title = "Second Activity"
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter(
            ContactIntentService.CONTACTINTENTSERVICE_RESPONSE
        ).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(contactReceiver, intentFilter)
        startService(Intent(this, ContactIntentService::class.java))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactReceiver)
    }

    private fun sendResult(intent: Intent?) {
        setResult(RESULT_OK, intent)
        finish()
    }

}