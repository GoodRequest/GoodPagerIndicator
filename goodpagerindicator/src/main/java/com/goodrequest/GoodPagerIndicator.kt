package com.goodrequest

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.*
import androidx.annotation.AttrRes
import com.goodrequest.base.SameChildCountPagerIndicator
import kotlin.math.max
import kotlin.math.min

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

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG.or(Paint.DITHER_FLAG)).apply {
    }
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

    /**
     * How many items from start won't be displayed
     */
    var ignoreFirstCount: Int = 0
        set(value) {
            field = value
            redraw()
        }

    /**
     * How many items from end won't be displayed
     */
    var ignoreLastCount: Int = 0
        set(value) {
            field = value
            redraw()
        }

    /**
     * If you currently visible page is in ignore bounds and this flag is set to true
     * not a single dot will be displayed on screen
     *
     * @see ignoreFirstCount
     * @see ignoreLastCount
     */
    var ignoreHides: Boolean = true
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
                ignoreFirstCount = getInt(R.styleable.GoodPagerIndicator_indicator_ignore_first_count, 0)
                ignoreLastCount = getInt(R.styleable.GoodPagerIndicator_indicator_ignore_first_count, 0)
                ignoreHides = getBoolean(R.styleable.GoodPagerIndicator_indicator_ignore_hides, true)
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
                val blurRadius = getDimension(R.styleable.GoodPagerIndicator_indicator_dot_blur_radius, 0f)
                if (blurRadius > 0f) dotPaint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.INNER)
            } finally {
                recycle()
            }
        }
    }

    override fun onScroll() {
        super.onScroll()
        for (i in 0 until ignoreFirstCount) {
            getChildAt(i)?.visibility = View.GONE
        }
        for (i in itemCount - ignoreLastCount..itemCount) {
            getChildAt(i)?.visibility = View.GONE
        }
    }

    override fun onMeasureDot(widthMeasureSpec: Int, heightMeasureSpec: Int): Pair<Int, Int> {
        val w = MeasureSpec.makeMeasureSpec((max(dotMinSize, dotMaxSize) * dotSizeFactor).toInt() + dotSpacing, MeasureSpec.EXACTLY)
        val h = MeasureSpec.makeMeasureSpec((max(dotMinSize, dotMaxSize) * dotSizeFactor).toInt(), MeasureSpec.EXACTLY)
        return w to h
    }

    override fun onDrawDot(canvas: Canvas, position: Int, width: Int, height: Int) {
        if (ignoreHides && (absolutePosition < ignoreFirstCount || absolutePosition > itemCount - ignoreLastCount - 1)) return
        val dist = getRelativeDistance(position)

        // dot color
        dotPaint.color = if (dist < 1) colorEvaluator.evaluate(dist, activeColor, inactiveColor) as Int else inactiveColor

        // dot size
        val minSize = min(dotMinSize, dotMaxSize).toFloat()
        val maxSize = max(dotMinSize, dotMaxSize).toFloat()

        val spanProgress = 1 - dist / resizingSpan
        val radius = if (dist > resizingSpan) minSize / 2 else ((maxSize - minSize) * spanProgress + minSize) / 2

        canvas.drawCircle(
            width.toFloat() / 2,
            height.toFloat() / 2,
            radius,
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
