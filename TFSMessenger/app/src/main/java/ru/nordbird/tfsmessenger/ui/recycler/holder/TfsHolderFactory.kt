package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.base.HolderFactory
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class TfsHolderFactory(
        private val clickListener: (ViewTyped, View, ClickedViewType) -> Unit,
        private val longClickListener: (ViewTyped, View, ClickedViewType) -> Unit
) : HolderFactory() {

    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder<*>? {
        return when (viewType) {
            R.layout.item_message_in -> MessageInViewHolder(view, clickListener, longClickListener)
            R.layout.item_message_out -> MessageOutViewHolder(view, clickListener, longClickListener)
            R.layout.item_separator_date -> SeparatorDateViewHolder(view)
            else -> null
        }
    }

}