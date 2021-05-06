package ru.nordbird.tfsmessenger.extensions

import androidx.fragment.app.Fragment

fun Fragment.isFinishing(): Boolean {
    return isRemoving || requireActivity().isFinishing
}