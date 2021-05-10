package ru.nordbird.tfsmessenger.extensions

import android.content.Context
import ru.nordbird.tfsmessenger.R

fun Throwable.userMessage(context: Context): String {
    val msg = message ?: context.getString(R.string.default_error_title)
    return if (msg.contains("HTTP 400")) {
        context.getString(R.string.error_http_400)
    } else if (msg.contains("HTTP 429")) {
        context.getString(R.string.error_http_429)
    } else if (msg.contains("Unable to resolve host")) {
        context.getString(R.string.error_server_unavailable)
    } else if (msg.contains("Query returned empty result")) {
        context.getString(R.string.default_error_title)
    } else {
        msg
    }
}
