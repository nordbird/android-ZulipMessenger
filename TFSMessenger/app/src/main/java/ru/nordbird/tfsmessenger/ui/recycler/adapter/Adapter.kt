package ru.nordbird.tfsmessenger.ui.recycler.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseAdapter
import ru.nordbird.tfsmessenger.ui.recycler.base.HolderFactory
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class Adapter<T : ViewTyped>(holderFactory: HolderFactory) : BaseAdapter<T>(holderFactory) {

    private val localItems: MutableList<T> = mutableListOf()

    override var items: List<T>
        get() = localItems
        set(newItems) {
            val diffCallback = object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean = localItems[oldPos].uid == newItems[newPos].uid

                override fun getOldListSize(): Int = localItems.size

                override fun getNewListSize(): Int = newItems.size

                override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean = localItems[oldPos].asString() == newItems[newPos].asString()
            }

            val diffResult = DiffUtil.calculateDiff(diffCallback)
            localItems.clear()
            localItems.addAll(newItems)
            diffResult.dispatchUpdatesTo(this)
        }

}