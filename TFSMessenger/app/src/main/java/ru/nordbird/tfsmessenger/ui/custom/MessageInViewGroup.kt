package ru.nordbird.tfsmessenger.ui.custom

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.extensions.layout

class MessageInViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val avatarView: CircleImageView
    private val messageBox: LinearLayout
    private val reactionBox: FlexBoxLayout
    private val attachmentView: ImageView

    private val avatarRect = Rect()
    private val messageBoxRect = Rect()
    private val reactionBoxRect = Rect()
    private val attachmentViewRect = Rect()

    init {
        LayoutInflater.from(context).inflate(R.layout.message_in_view_group, this, true)
        avatarView = findViewById(R.id.civ_avatar)
        messageBox = findViewById(R.id.ll_messageBox)
        reactionBox = findViewById(R.id.fbl_reaction)
        attachmentView = findViewById(R.id.iv_attachment)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var reactionBoxHeight = 0
        var reactionBoxWidth = 0
        var attachmentViewHeight = 0
        var attachmentViewWidth = 0

        val (avatarWidth, avatarHeight) =
            measureChilds(avatarView, widthMeasureSpec, 0, heightMeasureSpec, 0)
        val (messageBoxWidth, messageBoxHeight) =
            measureChilds(messageBox, widthMeasureSpec, avatarWidth, heightMeasureSpec, 0)

        if (attachmentView.visibility != GONE) {
            measureChilds(attachmentView, widthMeasureSpec, avatarWidth, heightMeasureSpec, messageBoxHeight).apply {
                attachmentViewWidth = first
                attachmentViewHeight = second
            }
        }

        if (reactionBox.visibility != GONE) {
            measureChilds(reactionBox, widthMeasureSpec, avatarWidth, heightMeasureSpec, messageBoxHeight + attachmentViewHeight).apply {
                reactionBoxWidth = first
                reactionBoxHeight = second
            }
        }

        setMeasuredDimension(
            resolveSize(
                avatarWidth + maxOf(messageBoxWidth, reactionBoxWidth, attachmentViewWidth) + paddingStart + paddingEnd,
                widthMeasureSpec
            ),
            resolveSize(
                maxOf(avatarHeight, messageBoxHeight + reactionBoxHeight + attachmentViewHeight) + paddingTop + paddingBottom,
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
        val reactionBoxLayoutParams = reactionBox.layoutParams as MarginLayoutParams
        val attachmentViewLayoutParams = attachmentView.layoutParams as MarginLayoutParams

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

        if (attachmentView.visibility != GONE) {
            with(attachmentViewRect) {
                left = attachmentViewLayoutParams.leftMargin + avatarRect.right + avatarLayoutParams.rightMargin
                top = messageBoxRect.bottom + attachmentViewLayoutParams.topMargin
                right = left + attachmentView.measuredWidth
                bottom = top + attachmentView.measuredHeight
            }
            attachmentView.layout(attachmentViewRect)
        }

        if (reactionBox.visibility != GONE) {
            with(reactionBoxRect) {
                left = reactionBoxLayoutParams.leftMargin + avatarRect.right + avatarLayoutParams.rightMargin
                top = attachmentViewRect.bottom + reactionBoxLayoutParams.topMargin
                right = reactionBoxRect.left + reactionBox.measuredWidth
                bottom = reactionBoxRect.top + reactionBox.measuredHeight
            }
            reactionBox.layout(reactionBoxRect)
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams =
        MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)

    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams = MarginLayoutParams(p)

}