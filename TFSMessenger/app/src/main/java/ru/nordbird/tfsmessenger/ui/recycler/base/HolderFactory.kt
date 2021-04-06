package ru.nordbird.tfsmessenger.ui.recycler.base

import android.view.View
import android.view.ViewGroup
import ru.nordbird.tfsmessenger.extensions.inflate

abstract class HolderFactory : (ViewGroup, Int) -> BaseViewHolder<ViewTyped> {

    abstract fun createViewHolder(view: View, viewType: Int): BaseViewHolder<*>?

    final override fun invoke(viewGroup: ViewGroup, viewType: Int): BaseViewHolder<ViewTyped> {
        val view: View = viewGroup.inflate(viewType)

        return createViewHolder(view, viewType) as BaseViewHolder<ViewTyped>
    }

}
