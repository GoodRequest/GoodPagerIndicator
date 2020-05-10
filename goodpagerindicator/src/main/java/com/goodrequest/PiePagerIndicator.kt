package com.goodrequest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.goodrequest.base.SingleChildPagerIndicator

@Suppress("MemberVisibilityCanBePrivate", "unused")
class PiePagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SingleChildPagerIndicator(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    override fun onMeasureDot(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        MeasureSpec.makeMeasureSpec(30, MeasureSpec.EXACTLY) to MeasureSpec.makeMeasureSpec(30, MeasureSpec.EXACTLY)

    override fun onDrawDot(canvas: Canvas, position: Int) {
        canvas.drawArc(
            0F,
            0F,
            30F,
            30F,
            0F,
            360 * progress,
            true,
            paint
        )
    }
}