package ru.nordbird.tfshomework1.service

import android.app.IntentService
import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ContactIntentService : IntentService("ContactIntentService") {

    companion object {
        const val CONTACTINTENTSERVICE_RESPONSE = "ru.nordbird.tfshomework1.service.RESPONSE"
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d("M_ContactIntentService", "Service started")
        val contacts = mutableListOf<String>()

        if (intent?.getIntExtra("permissionGranted", 0) == 1) {
            contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
                ?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val contactName = cursor
                            .getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        contacts.add(contactName)
                    }
                }
        }

        val responseIntent = Intent()
        responseIntent.action = CONTACTINTENTSERVICE_RESPONSE
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT)
        responseIntent.putExtra("result", contacts.toTypedArray())

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(responseIntent)
    }
}