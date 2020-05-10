package com.goodrequest

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.goodrequest.base.SingleChildPagerIndicator

private const val defaultPieColorAttrRes = android.R.attr.colorAccent
private const val defaultPieSizeDp = 16

@Suppress("MemberVisibilityCanBePrivate", "unused")
class PiePagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SingleChildPagerIndicator(context, attrs, defStyleAttr) {

    private val paint : Paint
    private val pieSize: Int
    private val startAngle: Float

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PiePagerIndicator,
            0, 0
        ).apply {
            try {
                val pieColor = getColor(R.styleable.PiePagerIndicator_indicator_pie_color, context.fetchColor(defaultPieColorAttrRes))
                pieSize = getDimensionPixelSize(R.styleable.PiePagerIndicator_indicator_pie_size, (defaultPieSizeDp * context.resources.displayMetrics.density).toInt())
                startAngle = getFloat(R.styleable.PiePagerIndicator_indicator_pie_start_angle, 0F)
                paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = pieColor
                    style = Paint.Style.FILL
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasureDot(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        MeasureSpec.makeMeasureSpec(pieSize, MeasureSpec.EXACTLY) to MeasureSpec.makeMeasureSpec(pieSize, MeasureSpec.EXACTLY)

    override fun onDrawDot(canvas: Canvas, position: Int) {
        canvas.drawArc(
            0F,
            0F,
            pieSize.toFloat(),
            pieSize.toFloat(),
            startAngle,
            360 * progress,
            true,
            paint
        )
    }

    private fun Context.fetchColor(@AttrRes resId: Int): Int {
        val typedValue = TypedValue()
        val a: TypedArray = theme.obtainStyledAttributes(typedValue.data, intArrayOf(resId))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }
}