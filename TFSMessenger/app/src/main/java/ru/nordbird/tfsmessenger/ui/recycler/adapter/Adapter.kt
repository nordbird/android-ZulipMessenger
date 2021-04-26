package ru.nordbird.tfsmessenger.ui.recycler.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseAdapter
import ru.nordbird.tfsmessenger.ui.recycler.base.HolderFactory
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class Adapter<T : ViewTyped>(
    holderFactory: HolderFactory,
    diffUtilCallback: DiffUtil.ItemCallback<T>
) : BaseAdapter<T>(holderFactory) {

    private val differ: AsyncListDiffer<T> = AsyncListDiffer(this, diffUtilCallback)

    override var items: List<T>
        get() = differ.currentList
        set(newItems) = differ.submitList(newItems)
}