package ru.nordbird.tfsmessenger.ui.recycler.holder

import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class UserShimmerUi(
    override val viewType: Int = R.layout.item_user_shimmer
) : ViewTyped {
    override val uid = "USER_SHIMMER_ITEM"
    override fun asString() = uid
}

