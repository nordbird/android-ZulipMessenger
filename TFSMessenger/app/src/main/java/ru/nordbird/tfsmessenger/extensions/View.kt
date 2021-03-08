package ru.nordbird.tfsmessenger.extensions

import android.graphics.Rect
import android.view.View

fun View.layout(rect: Rect) {
    layout(rect.left, rect.top, rect.right, rect.bottom)
}