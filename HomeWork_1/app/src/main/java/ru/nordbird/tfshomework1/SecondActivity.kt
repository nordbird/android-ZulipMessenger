package ru.nordbird.tfshomework1

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ru.nordbird.tfshomework1.service.ContactIntentService


class SecondActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PERMISSION_READ_CONTACTS = 1
    }

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

    override fun onStart() {
        super.onStart()

        val intentFilter = IntentFilter(
            ContactIntentService.CONTACTINTENTSERVICE_RESPONSE
        ).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(contactReceiver, intentFilter)

        if (contactPermissionGranted()) readContacts(true)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactReceiver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_READ_CONTACTS -> {
                readContacts(
                    grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                )
                return
            }
        }
    }

    private fun contactPermissionGranted(): Boolean {
        val permissionStatus =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_CONTACTS),
            REQUEST_CODE_PERMISSION_READ_CONTACTS
        )
        return false
    }

    private fun readContacts(permissionGranted: Boolean) {
        startService(Intent(this, ContactIntentService::class.java).apply {
            putExtra(
                "permissionGranted",
                if (permissionGranted) 1 else 0
            )
        })
    }

    private fun sendResult(intent: Intent?) {
        setResult(RESULT_OK, intent)
        finish()
    }
}