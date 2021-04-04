package ru.nordbird.tfsmessenger.ui.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.extensions.dpToPx

class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val DEFAULT_TEXT = "Hi"
        private const val DEFAULT_SIZE_DP = 40F

        private val bgColors = arrayOf(
            Color.parseColor("#7BC862"),
            Color.parseColor("#E17076"),
            Color.parseColor("#FAA774"),
            Color.parseColor("#6EC9CB"),
            Color.parseColor("#65AADD"),
            Color.parseColor("#A695E7"),
            Color.parseColor("#EE7AAE"),
            Color.parseColor("#2196F3")
        )
    }

    private var radius: Float = 0F
    private val viewRect = Rect()
    private lateinit var resultBm: Bitmap
    private lateinit var maskBm: Bitmap
    private lateinit var srcBm: Bitmap

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    var avatarDrawable: Drawable? = null
        set(value) {
            if (field != value) {
                field = value
                prepareBitmaps(width, height)
                invalidate()
            }
        }

    var text: String = DEFAULT_TEXT
        set(value) {
            val cut = value.substring(0, 2)
            if (field != cut) {
                field = cut
                requestLayout()
            }
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CircleImageView).apply {
            avatarDrawable = getDrawable(R.styleable.CircleImageView_civ_avatar)
            text = getText(R.styleable.CircleImageView_civ_text)?.toString() ?: DEFAULT_TEXT
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultSize = context.dpToPx(DEFAULT_SIZE_DP)
        val width = resolveSize(defaultSize, widthMeasureSpec)
        val height = resolveSize(defaultSize, heightMeasureSpec)

        val size = maxOf(width, height)
        radius = size / 2F
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val canvasCount = canvas.save()
        if (avatarDrawable == null) {
            drawText(canvas)
        } else {
            drawAvatar(canvas)
        }
        canvas.restoreToCount(canvasCount)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w == 0) return
        viewRect.set(0, 0, w, h)
        prepareBitmaps(w, h)
    }

    private fun drawText(canvas: Canvas) {
        circlePaint.color = initialsToColor(text)
        canvas.drawCircle(viewRect.exactCenterX(), viewRect.exactCenterY(), radius, circlePaint)

        textPaint.textSize = measuredHeight * 0.33f
        val offsetY = (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, viewRect.exactCenterX(), viewRect.exactCenterY() - offsetY, textPaint)
    }

    private fun drawAvatar(canvas: Canvas) {
        canvas.drawBitmap(resultBm, viewRect, viewRect, null)
    }

    private fun initialsToColor(letters: String): Int {
        val index = if (letters.isEmpty()) 0 else letters[0].toByte() % bgColors.size
        return bgColors[index]
    }

    private fun prepareBitmaps(w: Int, h: Int) {
        if (avatarDrawable == null || w == 0 || h == 0) return
        maskBm = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        resultBm = maskBm.copy(Bitmap.Config.ARGB_8888, true)

        val maskCanvas = Canvas(maskBm)
        val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        maskCanvas.drawOval(viewRect.toRectF(), avatarPaint)
        avatarPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        srcBm = avatarDrawable!!.toBitmap(w, h, Bitmap.Config.ARGB_8888)

        val resultCanvas = Canvas(resultBm)
        resultCanvas.drawBitmap(maskBm, viewRect, viewRect, null)
        resultCanvas.drawBitmap(srcBm, viewRect, viewRect, avatarPaint)
    }

}