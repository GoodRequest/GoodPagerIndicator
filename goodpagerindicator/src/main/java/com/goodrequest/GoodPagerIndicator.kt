package com.goodrequest

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.animation.*
import androidx.annotation.AttrRes
import kotlin.math.max

private const val linearInterpolator = 0
private const val accelerateInterpolator = 1
private const val decelerateInterpolator = 2
private const val bounceInterpolator = 3
private const val overshotInterpolator = 4

private const val defaultMinDotSizeDp = 4
private const val defaultMaxDotSizeDp = 12
private const val defaultDotSpacingDp = 4
private const val defaultSpanSize = 3
private const val defaultActiveColorAttrRes = android.R.attr.colorAccent
private const val defaultInactiveColorAttrRes = android.R.attr.colorPrimary
private const val defaultInterpolator = linearInterpolator

/**
 * Pager indicator, that contains exactly same amount of children as viewpager contains pages.
 *
 * It is most suitable to use if there won't be any connection or interaction shown between
 * children.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class GoodPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SameChildCountPagerIndicator(context, attrs, defStyleAttr) {

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colorEvaluator = ArgbEvaluator()

    var dotMinSize: Int = 0
        set(value) {
            field = value
            redraw()
        }

    var dotMaxSize: Int = 0
        set(value) {
            field = value
            redraw()
        }

    var dotSpacing: Int = 0
        set(value) {
            field = value
            redraw()
        }

    var resizingSpan: Int = 0
        set(value) {
            field = value
            redraw()
        }

    var activeColor: Int = 0
        set(value) {
            field = value
            redraw()
        }

    var inactiveColor: Int = 0
        set(value) {
            field = value
            redraw()
        }

    var interpolator: BaseInterpolator = LinearInterpolator()
        set(value) {
            field = value
            redraw()
        }

    var dotSizeFactor: Float = 1.0f
        set(value) {
            field = value
            redraw()
        }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GoodPagerIndicator,
            0, 0
        ).apply {
            try {
                dotMinSize = getDimensionPixelSize(R.styleable.GoodPagerIndicator_indicator_dot_min_size, (defaultMinDotSizeDp * context.resources.displayMetrics.density).toInt())
                dotMaxSize = getDimensionPixelSize(R.styleable.GoodPagerIndicator_indicator_dot_max_size, (defaultMaxDotSizeDp * context.resources.displayMetrics.density).toInt())
                dotSpacing = getDimensionPixelSize(R.styleable.GoodPagerIndicator_indicator_dot_spacing, (defaultDotSpacingDp * context.resources.displayMetrics.density).toInt())
                resizingSpan = getInt(R.styleable.GoodPagerIndicator_indicator_resizing_span, defaultSpanSize)
                activeColor = getColor(R.styleable.GoodPagerIndicator_indicator_dot_active_color, context.fetchColor(defaultActiveColorAttrRes))
                inactiveColor = getColor(R.styleable.GoodPagerIndicator_indicator_dot_inactive_color, context.fetchColor(defaultInactiveColorAttrRes))
                interpolator = when (getInt(R.styleable.GoodPagerIndicator_indicator_interpolator, defaultInterpolator)) {
                    linearInterpolator -> LinearInterpolator()
                    accelerateInterpolator -> AccelerateInterpolator()
                    decelerateInterpolator -> DecelerateInterpolator()
                    bounceInterpolator -> BounceInterpolator()
                    overshotInterpolator -> OvershootInterpolator()
                    else -> throw IllegalArgumentException("Select proper value from enum")
                }
                dotSizeFactor = getFloat(R.styleable.GoodPagerIndicator_indicator_dot_size_factor, 1.0f)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasureDot(widthMeasureSpec: Int, heightMeasureSpec: Int): Pair<Int, Int> {
        val w = MeasureSpec.makeMeasureSpec((max(dotMinSize, dotMaxSize) * dotSizeFactor).toInt() + dotSpacing, MeasureSpec.EXACTLY)
        val h = MeasureSpec.makeMeasureSpec((max(dotMinSize, dotMaxSize) * dotSizeFactor).toInt(), MeasureSpec.EXACTLY)
        return w to h
    }

    override fun onDrawDot(canvas: Canvas, distanceFactor: Float) {
        // dot color
        dotPaint.color = if (distanceFactor < 1) colorEvaluator.evaluate(distanceFactor, activeColor, inactiveColor) as Int else inactiveColor

        // dot size
        val progress = 1F - distanceFactor / resizingSpan
        val interpolatedProgress = interpolator.getInterpolation(progress)
        canvas.drawCircle(
            canvas.width.toFloat() / 2,
            canvas.height.toFloat() / 2,
            ((dotMaxSize - dotMinSize) * interpolatedProgress + dotMinSize) / 2,
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
