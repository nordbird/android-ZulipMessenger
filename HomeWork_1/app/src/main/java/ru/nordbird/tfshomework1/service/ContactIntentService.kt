package ru.nordbird.tfshomework1.service

import android.app.IntentService
import android.content.Intent
import android.util.Log

class ContactIntentService : IntentService("ContactIntentService") {

    val CONTACTINTENTSERVICE_RESPONSE = "ru.nordbird.tfshomework1.service.RESPONSE"

    override fun onHandleIntent(intent: Intent?) {
        Log.d("M_ContactIntentService", "Service started")
        val responseIntent = Intent()
        responseIntent.action = CONTACTINTENTSERVICE_RESPONSE
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT)

//        sendBroadcast(responseIntent)
    }
}