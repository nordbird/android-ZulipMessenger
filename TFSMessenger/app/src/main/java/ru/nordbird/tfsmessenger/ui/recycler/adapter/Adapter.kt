package ru.nordbird.tfsmessenger.ui.recycler.adapter

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.Observable
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseAdapter
import ru.nordbird.tfsmessenger.ui.recycler.base.DiffUtilCallback
import ru.nordbird.tfsmessenger.ui.recycler.base.HolderFactory
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class Adapter<T : ViewTyped>(holderFactory: HolderFactory) : BaseAdapter<T>(holderFactory) {

    private val localItems: MutableList<T> = mutableListOf()

    override var items: List<T>
        get() = localItems
        set(newItems) {
            Observable.combineLatest(Observable.fromArray(localItems), Observable.fromArray(newItems))
            { oldList, newList -> DiffUtilCallback(oldList, newList) }
                .map { DiffUtil.calculateDiff(it) }
                .doOnNext {
                    localItems.clear()
                    localItems.addAll(newItems)
                }
                .subscribe { it.dispatchUpdatesTo(this) }.dispose()
        }
}