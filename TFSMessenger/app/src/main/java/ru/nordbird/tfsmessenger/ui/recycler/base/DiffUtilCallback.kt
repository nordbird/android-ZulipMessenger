package ru.nordbird.tfsmessenger.ui.recycler.base

import androidx.recyclerview.widget.DiffUtil

class DiffUtilCallback(
    private val oldItems: List<ViewTyped>,
    private val newItems: List<ViewTyped>,
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean = oldItems[oldPos].uid == newItems[newPos].uid

    override fun getOldListSize(): Int = oldItems.size

    override fun getNewListSize(): Int = newItems.size

    override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean = oldItems[oldPos].asString() == newItems[newPos].asString()

}