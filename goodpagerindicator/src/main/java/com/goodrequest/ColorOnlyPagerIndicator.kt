package com.goodrequest

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.goodrequest.base.SameChildCountPagerIndicator

private const val defaultActiveColorAttrRes = android.R.attr.colorAccent
private const val defaultInactiveColorAttrRes = android.R.attr.colorPrimary
private const val defaultDotSizeDp = 12
private const val defaultDotPaddingDp = 2

private const val previewPosition = 3
private const val previewOffset = 0f
private const val previewItemCount = 6

@Suppress("MemberVisibilityCanBePrivate", "unused")
class ColorOnlyPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SameChildCountPagerIndicator(context, attrs, defStyleAttr) {

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colorEvaluator = ArgbEvaluator()

    val activeColor: Int
    val inactiveColor: Int
    val dotSize: Int
    val dotPadding: Int

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ColorOnlyPagerIndicator,
            0, 0
        ).apply {
            try {
                activeColor = getColor(R.styleable.ColorOnlyPagerIndicator_indicator_dot_active_color, context.fetchColor(defaultActiveColorAttrRes))
                inactiveColor = getColor(R.styleable.ColorOnlyPagerIndicator_indicator_dot_inactive_color, context.fetchColor(defaultInactiveColorAttrRes))
                dotSize = getDimensionPixelSize(R.styleable.ColorOnlyPagerIndicator_indicator_dot_size, (defaultDotSizeDp * context.resources.displayMetrics.density).toInt())
                dotPadding = getDimensionPixelSize(R.styleable.ColorOnlyPagerIndicator_indicator_dot_padding, (defaultDotPaddingDp * context.resources.displayMetrics.density).toInt())
            } finally {
                recycle()
            }
        }
        if (isInEditMode) {
            onScroll(previewItemCount, previewPosition, previewOffset)
        }
    }

    override fun onMeasureDot(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        MeasureSpec.makeMeasureSpec(dotSize, MeasureSpec.EXACTLY) to MeasureSpec.makeMeasureSpec(dotSize, MeasureSpec.EXACTLY)

    override fun onDrawDot(canvas: Canvas, position: Int) {
        val distance = getRelativeDistance(position)

        // dot color
        dotPaint.color = if (distance < 1) colorEvaluator.evaluate(
            distance,
            activeColor,
            inactiveColor
        ) as Int else inactiveColor

        // dot size
        canvas.drawCircle(
            canvas.width.toFloat() / 2,
            canvas.height.toFloat() / 2,
            canvas.width.toFloat() / 2 - dotPadding,
            dotPaint
        )
    }
}

/**
 * Utility function to obtain theme color based on attribute [resId]. For example
 * [android.R.attr.colorPrimary], [android.R.attr.colorPrimaryDark] or [android.R.attr.colorAccent]
 */
private fun Context.fetchColor(@AttrRes resId: Int): Int {
    val typedValue = TypedValue()
    val a: TypedArray = theme.obtainStyledAttributes(typedValue.data, intArrayOf(resId))
    val color = a.getColor(0, 0)
    a.recycle()
    return color
}
