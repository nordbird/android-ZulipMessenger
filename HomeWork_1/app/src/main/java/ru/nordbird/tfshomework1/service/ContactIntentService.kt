package ru.nordbird.tfshomework1.service

import android.app.IntentService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ContactIntentService : IntentService("ContactIntentService") {

    companion object {
        const val CONTACTINTENTSERVICE_RESPONSE = "ru.nordbird.tfshomework1.service.RESPONSE"
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d("M_ContactIntentService", "Service started")
        SystemClock.sleep(2000);

        val responseIntent = Intent()
        responseIntent.action = CONTACTINTENTSERVICE_RESPONSE
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT)
        responseIntent.putExtra("result", "DONE!")

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(responseIntent)
    }
}