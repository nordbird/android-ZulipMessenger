package ru.nordbird.tfsmessenger.ui.recycler.holder

import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class TopicShimmerUi(
    override val viewType: Int = R.layout.item_topic_shimmer
) : ViewTyped {
    override val uid = "TOPIC_SHIMMER_ITEM"
    override fun asString() = uid
}

