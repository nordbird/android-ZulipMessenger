package ru.nordbird.tfsmessenger.ui.recycler.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseAdapter
import ru.nordbird.tfsmessenger.ui.recycler.base.HolderFactory
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class Adapter<T : ViewTyped>(
    holderFactory: HolderFactory,
    diffUtilCallback: DiffUtil.ItemCallback<T>
) : BaseAdapter<T>(holderFactory) {

    val updateAction: Observable<List<T>> get() = updateRelay

    private val differ: AsyncListDiffer<T> = AsyncListDiffer(this, diffUtilCallback)

    private val listListener: AsyncListDiffer.ListListener<T> =
        AsyncListDiffer.ListListener<T> { _, currentList -> updateRelay.accept(currentList) }

    private val updateRelay: PublishRelay<List<T>> = PublishRelay.create()

    init {
        differ.addListListener(listListener)
    }

    override var items: List<T>
        get() = differ.currentList
        set(newItems) = differ.submitList(newItems)
}