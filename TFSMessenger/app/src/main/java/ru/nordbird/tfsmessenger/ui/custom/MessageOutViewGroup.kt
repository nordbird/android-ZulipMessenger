package ru.nordbird.tfsmessenger.ui.custom

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.extensions.layout

class MessageOutViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val messageView: TextView
    private val reactionBox: FlexBoxLayout

    private val messageBoxRect = Rect()
    private val reactionBoxRect = Rect()

    init {
        LayoutInflater.from(context).inflate(R.layout.message_out_view_group, this, true)
        messageView = findViewById(R.id.tv_message)
        reactionBox = findViewById(R.id.fbl_reaction)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var reactionBoxHeight = 0
        var reactionBoxWidth = 0

        val (messageBoxWidth, messageBoxHeight) =
            measureChilds(messageView, widthMeasureSpec, 0, heightMeasureSpec, 0)

        if (reactionBox.visibility != GONE) {
            measureChilds(reactionBox, widthMeasureSpec, 0, heightMeasureSpec, messageBoxHeight).apply {
                reactionBoxWidth = first
                reactionBoxHeight = second
            }
        }

        setMeasuredDimension(
            resolveSize(
                maxOf(messageBoxWidth, reactionBoxWidth) + paddingStart + paddingEnd,
                widthMeasureSpec
            ),
            resolveSize(
                messageBoxHeight + reactionBoxHeight + paddingTop + paddingBottom,
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
        val messageBoxLayoutParams = messageView.layoutParams as MarginLayoutParams
        val reactionBoxLayoutParams = reactionBox.layoutParams as MarginLayoutParams

        with(messageBoxRect) {
            right = measuredWidth - messageBoxLayoutParams.rightMargin
            left = right - messageView.measuredWidth
            top = paddingTop + messageBoxLayoutParams.topMargin
            bottom = top + messageView.measuredHeight
        }
        messageView.layout(messageBoxRect)

        if (reactionBox.visibility != GONE) {
            with(reactionBoxRect) {
                right = measuredWidth - reactionBoxLayoutParams.rightMargin
                left = right - reactionBox.measuredWidth
                top = messageBoxRect.height() + reactionBoxLayoutParams.topMargin
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