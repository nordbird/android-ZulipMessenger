package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.custom.CircleImageView
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class UserUi(
    val id: String,
    val name: String,
    val email: String,
    val isOnline: Boolean,
    override val viewType: Int = R.layout.item_user
) : ViewTyped {

    override val uid = id

    override fun asString() = "$name $email"

}

class UserViewHolder(
    view: View,
    private val clickListener: ViewHolderClickListener
) : BaseViewHolder<UserUi>(view) {

    private val userBox: ConstraintLayout = view.findViewById(R.id.userBox)
    private val avatarView: CircleImageView = view.findViewById(R.id.iv_user_avatar)
    private val nameView: TextView = view.findViewById(R.id.tv_user_name)
    private val emailView: TextView = view.findViewById(R.id.tv_user_email)
    private val onlineView: View = view.findViewById(R.id.indicator)

    init {
        userBox.setOnClickListener { v ->
            clickListener.onViewHolderClick(this, v)
        }
    }

    override fun bind(item: UserUi) {
        itemId = item.id
        nameView.text = item.name
        emailView.text = item.email
        onlineView.visibility = if (item.isOnline) View.VISIBLE else View.GONE
        avatarView.text = item.name
        super.bind(item)
    }

}
