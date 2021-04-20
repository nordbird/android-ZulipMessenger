package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.recycler.base.*

class TfsHolderFactory(
    private val currentUserId: Int = 0,
    private val clickListener: ViewHolderClickListener,
) : HolderFactory() {

    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_error -> ErrorViewHolder(view, clickListener)
            R.layout.item_message_in -> MessageInViewHolder(view, currentUserId, clickListener)
            R.layout.item_message_out -> MessageOutViewHolder(view, currentUserId, clickListener)
            R.layout.item_separator_date -> SeparatorDateViewHolder(view)
            R.layout.item_stream -> StreamViewHolder(view, clickListener)
            R.layout.item_topic -> TopicViewHolder(view, clickListener)
            R.layout.item_user -> UserViewHolder(view, clickListener)
            R.layout.item_stream_shimmer -> BaseViewHolder<StreamShimmerUi>(view)
            R.layout.item_user_shimmer -> BaseViewHolder<UserShimmerUi>(view)
            R.layout.item_topic_shimmer -> BaseViewHolder<TopicShimmerUi>(view)
            else -> throw RuntimeException("unknown viewType=" + view.resources.getResourceName(viewType))
        }
    }

}
