package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.ui.recycler.base.*

class TfsHolderFactory(
    private val currentUser: User,
    private val clickListener: ViewHolderClickListener,
) : HolderFactory() {

    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder<*>? {
        return when (viewType) {
            R.layout.item_message_in -> MessageInViewHolder(view, currentUser, clickListener)
            R.layout.item_message_out -> MessageOutViewHolder(view, currentUser, clickListener)
            R.layout.item_separator_date -> SeparatorDateViewHolder(view)
            else -> null
        }
    }

}