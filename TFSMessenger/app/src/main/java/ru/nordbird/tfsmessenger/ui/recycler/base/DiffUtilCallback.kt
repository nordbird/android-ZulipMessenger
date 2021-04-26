package ru.nordbird.tfsmessenger.ui.recycler.base

import androidx.recyclerview.widget.DiffUtil

class DiffUtilCallback<T : ViewTyped> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.asString() == newItem.asString()
    }

}