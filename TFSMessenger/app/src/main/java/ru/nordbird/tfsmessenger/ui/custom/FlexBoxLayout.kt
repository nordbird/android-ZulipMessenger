package ru.nordbird.tfsmessenger.ui.custom

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.children
import androidx.core.view.setPadding
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.extensions.dpToPx
import ru.nordbird.tfsmessenger.extensions.layout

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val DEFAULT_ICON_SIZE_DP = 16F
        private const val ADDITIONAL_PADDING = 8F
    }

    private var iconSize: Int = context.dpToPx(DEFAULT_ICON_SIZE_DP)
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    private var btnAddView: ImageView = ImageView(context)
    private var iconId = 0
    private var backgroundId = 0
    private val emojiRect = Rect()
    private val btnAddRect = Rect()

    init {
        setWillNotDraw(true)

        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.FlexBoxLayout).apply {
                iconSize = getDimensionPixelSize(
                    R.styleable.FlexBoxLayout_fbl_btnAddIconSize, context.dpToPx(
                        DEFAULT_ICON_SIZE_DP
                    )
                )

                iconId = getResourceId(R.styleable.FlexBoxLayout_fbl_btnAddIcon, 0)
                backgroundId = getResourceId(R.styleable.FlexBoxLayout_fbl_btnAddBackground, 0)

                recycle()
            }
        }

        addButtonAddView()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (btnAddView != children.last()) {
            removeView(btnAddView)
            addView(btnAddView)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var currentWidth = 0
        var maxWidth = 0
        var maxHeight = 0
        val specSize = MeasureSpec.getSize(widthMeasureSpec)

        children.forEach { child ->
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)

            val layoutParams = child.layoutParams as MarginLayoutParams
            val childWidth =
                child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
            val childHeight =
                child.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin

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
        var leftMargin = 0
        var topMargin = 0
        var bottomMargin = 0

        children.filter { it != btnAddView }.forEach { child ->
            val layoutParams = child.layoutParams as MarginLayoutParams
            leftMargin = layoutParams.leftMargin
            topMargin = layoutParams.topMargin
            bottomMargin = layoutParams.bottomMargin

            if (widthOffset + child.measuredWidth + leftMargin > width) {
                widthOffset = paddingStart
                heightOffset += child.measuredHeight + topMargin + bottomMargin
            }

            emojiRect.left = widthOffset + leftMargin
            emojiRect.top = heightOffset + topMargin
            emojiRect.right = emojiRect.left + child.measuredWidth
            emojiRect.bottom = emojiRect.top + child.measuredHeight
            child.layout(emojiRect)

            widthOffset += child.measuredWidth + leftMargin + layoutParams.rightMargin
        }

        // Сделаем кнопку "+" такого же размера как EmojiView если есть
        val btnSize = if (childCount > 1) emojiRect.height() else btnAddView.measuredHeight

        if (widthOffset + btnSize + leftMargin > width) {
            widthOffset = paddingStart
            heightOffset += btnSize + topMargin + bottomMargin
        }

        btnAddRect.left = widthOffset + leftMargin
        btnAddRect.top = heightOffset + topMargin
        btnAddRect.right = btnAddRect.left + btnSize
        btnAddRect.bottom = btnAddRect.top + btnSize
        btnAddView.layout(btnAddRect)
    }

    override fun generateDefaultLayoutParams(): LayoutParams =
        MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)

    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams = MarginLayoutParams(p)

    private fun addButtonAddView() {
        btnAddView.setImageResource(iconId)
        val bitmap = btnAddView.drawable?.toBitmap(iconSize, iconSize)
        btnAddView.setImageBitmap(bitmap)
        btnAddView.setBackgroundResource(backgroundId)
        btnAddView.setPadding(context.dpToPx(ADDITIONAL_PADDING))
        val layoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        btnAddView.layoutParams = layoutParams

        addView(btnAddView)
    }
}
