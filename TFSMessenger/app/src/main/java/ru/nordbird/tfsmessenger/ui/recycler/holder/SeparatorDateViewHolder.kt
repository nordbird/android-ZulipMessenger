package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.TextView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.extensions.format
import ru.nordbird.tfsmessenger.extensions.toZeroTime
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import java.util.*

class SeparatorDateUi(
        val date: Date,
        override val viewType: Int = R.layout.item_separator_date
) : ViewTyped {

    override val uid: String
        get() = date.toZeroTime().toString()
}

class SeparatorDateViewHolder(
        view: View
) : BaseViewHolder<SeparatorDateUi>(view) {

    private val dateView: TextView = view.findViewById(R.id.tv_separator_date)

    override fun bind(item: SeparatorDateUi) {
        dateView.text = item.date.format("dd MMMM")
    }

}
