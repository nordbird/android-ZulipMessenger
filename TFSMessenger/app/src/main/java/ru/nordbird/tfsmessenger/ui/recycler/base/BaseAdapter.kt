package ru.nordbird.tfsmessenger.ui.recycler.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : ViewTyped>(internal val holderFactory: HolderFactory) : RecyclerView.Adapter<BaseViewHolder<ViewTyped>>() {

    abstract var items: List<T>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ViewTyped> = holderFactory(parent, viewType)

    override fun onBindViewHolder(holder: BaseViewHolder<ViewTyped>, position: Int) = holder.bind(items[position])

    override fun onBindViewHolder(holder: BaseViewHolder<ViewTyped>, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            holder.bind(items[position], payloads)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemViewType(position: Int): Int = items[position].viewType

    override fun getItemCount(): Int = items.size

    fun isEmpty() = items.isEmpty()

}