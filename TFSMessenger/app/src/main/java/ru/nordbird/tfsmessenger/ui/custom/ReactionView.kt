package ru.nordbird.tfsmessenger.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.extensions.spToPx

class ReactionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val DEFAULT_FONT_SIZE_SP = 14F
        private const val DEFAULT_FONT_COLOR = Color.BLACK
        private const val REACTION_DEFAULT_CODE = ""
        private const val REACTION_DEFAULT_COUNT = 0
        private val DRAWABLES_STATE = IntArray(1) { android.R.attr.state_selected }
    }

    var reactionCode: String = REACTION_DEFAULT_CODE
        set(value) {
            if (field != value) {
                field = value
                updateText()
            }
        }

    var reactionCount: Int = REACTION_DEFAULT_COUNT
        set(value) {
            if (field != value) {
                field = value
                updateText()
            }
        }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_FONT_COLOR
        textAlign = Paint.Align.CENTER
    }

    private var textSize: Int
        get() = textPaint.textSize.toInt()
        set(value) {
            if (textPaint.textSize.toInt() != value) {
                textPaint.textSize = value.toFloat()
                requestLayout()
            }
        }

    private var textColor: Int
        get() = textPaint.color
        set(value) {
            if (textPaint.color != value) {
                textPaint.color = value
                invalidate()
            }
        }

    private var text = ""
    private val textPoint = PointF()
    private val textBounds = Rect()

    init {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.ReactionView).apply {
                textSize = getDimensionPixelSize(
                    R.styleable.ReactionView_rv_textSize, context.spToPx(
                        DEFAULT_FONT_SIZE_SP
                    )
                )
                textColor = getColor(R.styleable.ReactionView_rv_textColor, DEFAULT_FONT_COLOR)
                reactionCode = getString(R.styleable.ReactionView_rv_reactionCode) ?: REACTION_DEFAULT_CODE
                reactionCount = getInt(R.styleable.ReactionView_rv_reactionCount, REACTION_DEFAULT_COUNT)
                isSelected = getBoolean(R.styleable.ReactionView_rv_selected, false)

                recycle()
            }
        }
        updateText()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val contentWidth = textBounds.width() + paddingStart + paddingEnd
        val contentHeight = textBounds.height() + paddingTop + paddingBottom
        val width = resolveContentSize(contentWidth, widthMeasureSpec)
        val height = resolveContentSize(contentHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val emptySpaceVertical = textPaint.descent() + textPaint.ascent()
        textPoint.set(measuredWidth / 2F, (measuredHeight - emptySpaceVertical) / 2)
    }

    override fun onDraw(canvas: Canvas) {
        val canvasCount = canvas.save()
        canvas.drawText(text, textPoint.x, textPoint.y, textPaint)
        canvas.restoreToCount(canvasCount)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isSelected) {
            mergeDrawableStates(drawableState, DRAWABLES_STATE)
        }
        return drawableState
    }

    private fun resolveContentSize(contentSize: Int, measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        return when (specMode) {
            MeasureSpec.EXACTLY -> maxOf(contentSize, specSize)
            else -> contentSize
        }
    }

    private fun updateText() {
        text = "$reactionCode $reactionCount"
        requestLayout()
    }
}