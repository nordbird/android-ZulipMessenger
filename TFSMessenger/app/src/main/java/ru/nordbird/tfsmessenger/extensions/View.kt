package ru.nordbird.tfsmessenger.extensions

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun View.layout(rect: Rect) {
    layout(rect.left, rect.top, rect.right, rect.bottom)
}

fun <T : View> View.inflate(
    @LayoutRes
    layout: Int,
    root: ViewGroup? = this as? ViewGroup,
    attachToRoot: Boolean = false
): T {
    return LayoutInflater.from(context).inflate(layout, root, attachToRoot) as T
}