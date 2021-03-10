package ru.nordbird.tfsmessenger.ui.custom

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.extensions.layout

class MessageViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val avatarView: CircleImageView
    private val messageBox: LinearLayout
    private val emojiBox: FlexBoxLayout

    private val avatarRect = Rect()
    private val messageBoxRect = Rect()
    private val emojiBoxRect = Rect()

    init {
        LayoutInflater.from(context).inflate(R.layout.message_view_group, this, true)
        avatarView = findViewById(R.id.civ_avatar)
        messageBox = findViewById(R.id.ll_messageBox)
        emojiBox = findViewById(R.id.fbl_emoji)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var emojiBoxHeight = 0
        var emojiBoxWidth = 0

        val (avatarWidth, avatarHeight) =
            measureChilds(avatarView, widthMeasureSpec, 0, heightMeasureSpec, 0)
        val (messageBoxWidth, messageBoxHeight) =
            measureChilds(messageBox, widthMeasureSpec, avatarWidth, heightMeasureSpec, 0)

        if (emojiBox.visibility != GONE) {
            measureChilds(emojiBox, widthMeasureSpec, avatarWidth, heightMeasureSpec, messageBoxHeight).apply {
                emojiBoxWidth = first
                emojiBoxHeight = second
            }
        }

        setMeasuredDimension(
            resolveSize(
                avatarWidth + maxOf(messageBoxWidth, emojiBoxWidth) + paddingStart + paddingEnd,
                widthMeasureSpec
            ),
            resolveSize(
                maxOf(avatarHeight, messageBoxHeight + emojiBoxHeight) + paddingTop + paddingBottom,
                heightMeasureSpec
            )
        )
    }

    private fun measureChilds(
        view: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Pair<Int, Int> {
        measureChildWithMargins(view, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed)
        val layoutParams = view.layoutParams as MarginLayoutParams
        val viewHeight = view.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
        val viewWidth = view.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin

        return viewWidth to viewHeight
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val avatarLayoutParams = avatarView.layoutParams as MarginLayoutParams
        val messageBoxLayoutParams = messageBox.layoutParams as MarginLayoutParams
        val emojiBoxLayoutParams = emojiBox.layoutParams as MarginLayoutParams

        with(avatarRect) {
            left = paddingStart + avatarLayoutParams.leftMargin
            top = paddingTop + avatarLayoutParams.topMargin
            right = avatarRect.left + avatarView.measuredWidth
            bottom = avatarRect.top + avatarView.measuredHeight
        }
        avatarView.layout(avatarRect)

        with(messageBoxRect) {
            left = messageBoxLayoutParams.leftMargin + avatarRect.right + avatarLayoutParams.rightMargin
            top = paddingTop + messageBoxLayoutParams.topMargin
            right = messageBoxRect.left + messageBox.measuredWidth
            bottom = messageBoxRect.top + messageBox.measuredHeight
        }
        messageBox.layout(messageBoxRect)

        if (emojiBox.visibility != GONE) {
            with(emojiBoxRect) {
                left = emojiBoxLayoutParams.leftMargin + avatarRect.right + avatarLayoutParams.rightMargin
                top = messageBoxRect.height() + emojiBoxLayoutParams.topMargin
                right = emojiBoxRect.left + emojiBox.measuredWidth
                bottom = emojiBoxRect.top + emojiBox.measuredHeight
            }
            emojiBox.layout(emojiBoxRect)
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams =
        MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)

    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams = MarginLayoutParams(p)

}