package ru.nordbird.tfsmessenger.ui.custom

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.core.view.children
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.extensions.layout

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    val btnAddView: ImageView = ImageView(context)

    private var iconId = 0
    private var backgroundId = 0
    private val childRect = Rect()

    fun getChilds(): Sequence<View> = children.filter { it != btnAddView }

    init {
        setWillNotDraw(true)

        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.FlexBoxLayout).apply {
                iconId = getResourceId(R.styleable.FlexBoxLayout_fbl_btnAddIcon, 0)
                backgroundId = getResourceId(R.styleable.FlexBoxLayout_fbl_btnAddBackground, 0)

                recycle()
            }
        }
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (child != btnAddView) {
            if (!children.contains(btnAddView)) {
                addView(btnAddView)
                setupButtonAdd()
            } else if (btnAddView != children.last()) {
                removeView(btnAddView)
                addView(btnAddView)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var currentWidth = 0
        var maxWidth = 0
        var maxHeight = 0
        var childWidth: Int
        var childHeight = 0
        val specSize = MeasureSpec.getSize(widthMeasureSpec)

        children.forEach { child ->
            if (child != btnAddView) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)

                val layoutParams = child.layoutParams as MarginLayoutParams
                childWidth =
                    child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
                childHeight =
                    child.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
            } else {
                // Сделаем кнопку "+" такого же размера как EmojiView по высоте
                childWidth = childHeight
            }

            currentWidth += childWidth
            if (currentWidth > specSize) {
                maxWidth = specSize
                currentWidth = childWidth
                maxHeight += childHeight
            }

            maxWidth = maxOf(maxWidth, currentWidth)
            maxHeight = maxOf(maxHeight, childHeight)
        }

        setMeasuredDimension(
            resolveSize(maxWidth + paddingStart + paddingEnd, widthMeasureSpec),
            resolveSize(maxHeight + paddingTop + paddingBottom, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var widthOffset = paddingStart
        var heightOffset = paddingTop
        var childWidth: Int
        var childHeight = 0
        var leftMargin = 0
        var topMargin = 0
        var bottomMargin = 0
        var rightMargin = 0

        children.forEach { child ->
            if (child != btnAddView) {
                childWidth = child.measuredWidth
                childHeight = child.measuredHeight
                val layoutParams = child.layoutParams as MarginLayoutParams
                leftMargin = layoutParams.leftMargin
                topMargin = layoutParams.topMargin
                bottomMargin = layoutParams.bottomMargin
                rightMargin = layoutParams.rightMargin
            } else {
                // Сделаем кнопку "+" такого же размера как EmojiView по высоте
                childWidth = childHeight
            }

            if (widthOffset + childWidth + leftMargin > measuredWidth) {
                widthOffset = paddingStart
                heightOffset += childHeight + topMargin + bottomMargin
            }

            childRect.left = widthOffset + leftMargin
            childRect.top = heightOffset + topMargin
            childRect.right = childRect.left + childWidth
            childRect.bottom = childRect.top + childHeight
            child.layout(childRect)

            widthOffset += child.measuredWidth + leftMargin + rightMargin
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams =
        MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)

    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams = MarginLayoutParams(p)

    private fun setupButtonAdd() {
        val emojiView = children.first { it != btnAddView }
        val viewLayoutParams = emojiView.layoutParams as MarginLayoutParams

        btnAddView.setImageResource(iconId)
        btnAddView.setBackgroundResource(backgroundId)
        btnAddView.setPadding(
            emojiView.paddingLeft, emojiView.paddingTop,
            emojiView.paddingRight, emojiView.paddingBottom
        )
        val btnLayoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        btnLayoutParams.setMargins(
            viewLayoutParams.leftMargin, viewLayoutParams.topMargin,
            viewLayoutParams.rightMargin, viewLayoutParams.bottomMargin
        )
        btnAddView.layoutParams = btnLayoutParams
    }
}
