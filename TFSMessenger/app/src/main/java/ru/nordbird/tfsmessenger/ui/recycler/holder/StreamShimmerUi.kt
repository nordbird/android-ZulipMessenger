package ru.nordbird.tfsmessenger.ui.recycler.holder

import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class StreamShimmerUi(
    override val viewType: Int = R.layout.item_stream_shimmer
) : ViewTyped {
    override val uid = "STREAM_SHIMMER_ITEM"
    override fun asString() = uid
}

