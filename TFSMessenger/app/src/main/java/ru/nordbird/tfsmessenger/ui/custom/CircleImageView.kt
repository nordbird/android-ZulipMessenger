package ru.nordbird.tfsmessenger.ui.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.extensions.spToPx


class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        style = Paint.Style.FILL_AND_STROKE
    }

    private val viewRect = Rect()
    private lateinit var resultBm: Bitmap
    private lateinit var maskBm: Bitmap
    private lateinit var srcBm: Bitmap

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        style = Paint.Style.FILL_AND_STROKE
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }
    private var foregroundDrawable: Drawable? = null
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    private var textSize: Int
        get() = textPaint.textSize.toInt()
        set(value) {
            if (textPaint.textSize.toInt() != value) {
                textPaint.textSize = value.toFloat()
                requestLayout()
            }
        }
    private var text: String = TEXT
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    private val centerPoint = PointF()
    private val textPoint = PointF()
    private var radius: Float = 0F
    private val textBounds = Rect()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CircleImageView).apply {
            textSize = getDimensionPixelSize(
                R.styleable.CircleImageView_civ_textSize, context.spToPx(
                    DEFAULT_FONT_SIZE_PX
                )
            )
            foregroundDrawable = getDrawable(R.styleable.CircleImageView_civ_foreground)
            text = getText(R.styleable.CircleImageView_civ_text)?.toString() ?: TEXT
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textWidth = textBounds.width()
        val textHeight = textBounds.height()
        val contentWidth = textWidth + paddingStart + paddingEnd
        val contentHeight = textHeight + paddingTop + paddingBottom

        val width = resolveSize(contentWidth, widthMeasureSpec)
        val height = resolveSize(contentHeight, heightMeasureSpec)

        val size = maxOf(width, height)
        radius = size / 2F
        setMeasuredDimension(size, size)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        centerPoint.set(radius, radius)
        val emptySpaceVertical = height - textBounds.height()
        textPoint.set(
            radius,
            emptySpaceVertical / 2f + textBounds.height()
        )
    }

    override fun onDraw(canvas: Canvas) {
        val canvasCount = canvas.save()
        if (foregroundDrawable == null) {
            canvas.drawCircle(centerPoint.x, centerPoint.y, radius, circlePaint)
            canvas.drawText(text, textPoint.x, textPoint.y, textPaint)
        } else {
            canvas.drawBitmap(resultBm, viewRect, viewRect, null)
        }
        canvas.restoreToCount(canvasCount)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w == 0) return

        with(viewRect) {
            left = 0
            top = 0
            right = w
            bottom = h
        }

        prepareBitmaps(w, h)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isSelected) {
            mergeDrawableStates(drawableState, DRAWABLES_STATE)
        }
        return drawableState
    }

    private fun prepareBitmaps(w: Int, h: Int) {
        if (foregroundDrawable == null) return
        maskBm = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        resultBm = maskBm.copy(Bitmap.Config.ARGB_8888, true)

        val maskCanvas = Canvas(maskBm)
        maskCanvas.drawOval(viewRect.toRectF(), maskPaint)
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        srcBm = foregroundDrawable!!.toBitmap(w, h, Bitmap.Config.ARGB_8888)

        val resultCanvas = Canvas(resultBm)
        resultCanvas.drawBitmap(maskBm, viewRect, viewRect, null)
        resultCanvas.drawBitmap(srcBm, viewRect, viewRect, maskPaint)
    }

    companion object {
        private const val TEXT = "Hi"
        private const val DEFAULT_FONT_SIZE_PX = 14F
        private val DRAWABLES_STATE = IntArray(1) { android.R.attr.state_selected }
    }

}