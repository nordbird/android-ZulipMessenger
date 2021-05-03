package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.extensions.SECOND
import ru.nordbird.tfsmessenger.ui.custom.CircleImageView
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import java.util.*

enum class UserPresence {
    ACTIVE,
    IDLE,
    OFFLINE
}

class UserUi(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String,
    private val timestamp_sec: Long,
    override val viewType: Int = R.layout.item_user
) : ViewTyped {

    override val uid = id.toString()

    override fun asString() = "$name $email ${presence.ordinal}"

    val presence: UserPresence
        get() {
            val now = Date().time / SECOND
            return when (now - timestamp_sec) {
                in 0..ACTIVE_MAX_TIME_SEC -> UserPresence.ACTIVE
                in ACTIVE_MAX_TIME_SEC + 1..IDLE_MAX_TIME_SEC -> UserPresence.IDLE
                else -> UserPresence.OFFLINE
            }
        }

    companion object {
        private const val ACTIVE_MAX_TIME_SEC = 600
        private const val IDLE_MAX_TIME_SEC = 1800
    }
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
    private val target = object : CustomViewTarget<CircleImageView, Drawable>(avatarView) {
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            avatarView.avatarDrawable = resource
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            avatarView.avatarDrawable = null
        }

        override fun onResourceCleared(placeholder: Drawable?) {
            avatarView.avatarDrawable = null
        }
    }

    init {
        userBox.setOnClickListener { v ->
            clickListener.onViewHolderClick(this, v)
        }
    }

    override fun bind(item: UserUi) {
        nameView.text = item.name
        emailView.text = item.email

        val indicatorBg = if (item.presence == UserPresence.ACTIVE) R.drawable.bg_indicator_active else R.drawable.bg_indicator_idle
        onlineView.setBackgroundResource(indicatorBg)
        onlineView.visibility = if (item.presence != UserPresence.OFFLINE) View.VISIBLE else View.GONE

        avatarView.text = item.name

        if (item.avatar.isBlank()) {
            Glide.with(itemView)
                .clear(target)
        } else {
            Glide.with(itemView)
                .load(item.avatar)
                .into(target)
        }

        super.bind(item)
    }
}
