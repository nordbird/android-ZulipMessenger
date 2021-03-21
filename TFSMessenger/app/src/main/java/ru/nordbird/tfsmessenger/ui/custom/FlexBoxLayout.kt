package ru.nordbird.tfsmessenger.ui.custom

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
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

    private var gravity = Gravity.START
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private var iconId = 0
    private var backgroundId = 0
    private val childRect = Rect()
    private val childMargin = Rect()

    fun getChilds(): Sequence<View> = children.filter { it != btnAddView }

    init {
        setWillNotDraw(true)

        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.FlexBoxLayout).apply {
                iconId = getResourceId(R.styleable.FlexBoxLayout_fbl_btnAddIcon, 0)
                backgroundId = getResourceId(R.styleable.FlexBoxLayout_fbl_btnAddBackground, 0)
                gravity = getInteger(R.styleable.FlexBoxLayout_android_gravity, Gravity.START)

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
                childMargin.set(
                        layoutParams.leftMargin, layoutParams.topMargin,
                        layoutParams.rightMargin, layoutParams.bottomMargin
                )
                childWidth = child.measuredWidth
                childHeight = child.measuredHeight
            } else {
                // Сделаем кнопку "+" такого же размера как ReactionView по высоте
                childWidth = childHeight
            }

            val fullChildWidth = childWidth + childMargin.left + childMargin.right
            val fullChildHeight = childHeight + childMargin.top + childMargin.bottom

            currentWidth += fullChildWidth
            if (currentWidth > specSize) {
                maxWidth = specSize
                currentWidth = fullChildWidth
                maxHeight += fullChildHeight
            }

            maxWidth = maxOf(maxWidth, currentWidth)
            maxHeight = maxOf(maxHeight, fullChildHeight)
        }

        setMeasuredDimension(
                resolveSize(maxWidth + paddingStart + paddingEnd, widthMeasureSpec),
                resolveSize(maxHeight + paddingTop + paddingBottom, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount < 2) return

        var widthOffset = paddingStart
        var heightOffset = paddingTop
        var childWidth: Int
        var childHeight = 0
        var leftMargin = 0
        var topMargin = 0
        var bottomMargin = 0
        var rightMargin = 0

        val lastRowList = if (gravity == Gravity.END) {
            getLastRowViews()
        } else {
            emptyList<View>()
        }

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
                // Сделаем кнопку "+" такого же размера как ReactionView по высоте
                childWidth = childHeight
            }

            if (widthOffset + childWidth + leftMargin + rightMargin > measuredWidth) {
                if (!lastRowList.contains(child)) {
                    widthOffset = paddingStart
                } else {
                    // Если это последняя строка - начнем ее там где закончилась предыдущая
                    widthOffset = measuredWidth - widthOffset
                }
                heightOffset += childHeight + topMargin + bottomMargin
            }

            childRect.top = heightOffset + topMargin
            childRect.bottom = childRect.top + childHeight

            // Последняя строка будет идти независимо от Gravity
            if (gravity == Gravity.START || lastRowList.contains(child)) {
                childRect.left = widthOffset + leftMargin
                childRect.right = childRect.left + childWidth
            } else {
                childRect.right = measuredWidth - widthOffset
                childRect.left = childRect.right - childWidth
            }

            child.layout(childRect)

            widthOffset += childWidth + leftMargin + rightMargin
        }
    }

    private fun getLastRowViews(): MutableList<View> {
        val list = mutableListOf<View>()

        var widthOffset = paddingStart
        var childWidth: Int
        var childHeight = 0
        var leftMargin = 0
        var rightMargin = 0

        children.forEach { child ->
            if (child != btnAddView) {
                childWidth = child.measuredWidth
                childHeight = child.measuredHeight
                val layoutParams = child.layoutParams as MarginLayoutParams
                leftMargin = layoutParams.leftMargin
                rightMargin = layoutParams.rightMargin
            } else {
                // Сделаем кнопку "+" такого же размера как ReactionView по высоте
                childWidth = childHeight
            }

            if (widthOffset + childWidth + leftMargin > measuredWidth) {
                widthOffset = paddingStart
                list.clear()
            }

            list.add(child)
            widthOffset += childWidth + leftMargin + rightMargin
        }

        return list
    }

    override fun generateDefaultLayoutParams(): LayoutParams =
            MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)

    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams = MarginLayoutParams(p)

    private fun setupButtonAdd() {
        val reactionView = children.first { it != btnAddView }
        val viewLayoutParams = reactionView.layoutParams as MarginLayoutParams

        btnAddView.setImageResource(iconId)
        btnAddView.setBackgroundResource(backgroundId)
        btnAddView.setPadding(
                reactionView.paddingLeft, reactionView.paddingTop,
                reactionView.paddingRight, reactionView.paddingBottom
        )
        val btnLayoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        btnLayoutParams.setMargins(
                viewLayoutParams.leftMargin, viewLayoutParams.topMargin,
                viewLayoutParams.rightMargin, viewLayoutParams.bottomMargin
        )
        btnAddView.layoutParams = btnLayoutParams
    }
}
