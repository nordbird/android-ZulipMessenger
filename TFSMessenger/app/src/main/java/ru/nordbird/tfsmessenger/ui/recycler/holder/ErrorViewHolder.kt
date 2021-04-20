package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatButton
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class ErrorUi(
    @StringRes val title: Int? = null,
    override val viewType: Int = R.layout.item_error
) : ViewTyped {
    override val uid = "ERROR_ITEM"
    override fun asString() = uid
}

class ErrorViewHolder(
    view: View,
    private val clickListener: ViewHolderClickListener
) : BaseViewHolder<ErrorUi>(view) {

    private val titleView: TextView = view.findViewById(R.id.tv_error_title)
    private val buttonView: AppCompatButton = view.findViewById(R.id.btn_error_reload)

    init {
        buttonView.setOnClickListener { v ->
            clickListener.onViewHolderClick(this, v)
        }
    }

    override fun bind(item: ErrorUi) {
        titleView.setText(item.title ?: R.string.default_error_tilte)
        super.bind(item)
    }

}