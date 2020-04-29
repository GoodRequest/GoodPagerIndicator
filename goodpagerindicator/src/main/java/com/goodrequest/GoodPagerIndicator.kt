package com.goodrequest

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.*
import androidx.annotation.AttrRes
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.absoluteValue
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

@Suppress("MemberVisibilityCanBePrivate", "unused")
class GoodPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseGoodPagerIndicator(context, attrs, defStyleAttr) {

    var dotMinSize: Int = 0
        set(value) {
            field = value
            resetChildrenAttributes()
        }

    var dotMaxSize: Int = 0
        set(value) {
            field = value
            resetChildrenAttributes()
        }

    var dotSpacing: Int = 0
        set(value) {
            field = value
            resetChildrenAttributes()
        }

    var resizingSpan: Int = 0
        set(value) {
            field = value
            redrawChildren()
            redraw()
        }

    var activeColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var inactiveColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var interpolator: BaseInterpolator = LinearInterpolator()
        set(value) {
            field = value
            redrawChildren()
            redraw()
        }

    /**
     * This parameter will extend drawing boundaries multiplying them by this factor.
     * If your dot wants to take 100px and the factor is set to 2.0, the dot will have
     * 200px available and will be drawn in center of available space.
     *
     * Size factor is useful especially if your [interpolator] can return values greater then
     * 1.0. In such case [Dot] will draw outside its boundaries.
     *
     * It is recommended to use [dotSpacing] and padding instead of [dotSizeFactor]
     * if your interpolator do not return values greater then 1.0
     */
    var dotSizeFactor: Float = 1.0f
        set(value) {
            field = value
            invalidate()
        }

    private val colorEvaluator = ArgbEvaluator()

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
                swipeEnabled = getBoolean(R.styleable.GoodPagerIndicator_indicator_swipe_enabled, true)
                clickEnabled = getBoolean(R.styleable.GoodPagerIndicator_indicator_click_enabled, true)
                dotSizeFactor = getFloat(R.styleable.GoodPagerIndicator_indicator_dot_size_factor, 1.0f)
            } finally {
                recycle()
            }
        }
    }

    override fun onScroll(itemCount: Int, position: Int, positionOffset: Float) {
        if (itemCount != childCount || isInEditMode) {
            redrawChildren(itemCount)
        }
        redrawProgress(position, positionOffset)
    }

    // computation section
    /**
     * Redraw children based on newly set [position] and [positionOffset]. If [force]
     * is set to true, the dots will be forcefully redrawn (removed and added). Do not
     * call this function with [force] set to true too often, since it is resource
     * consuming operation (call it in setters etc.)
     *
     * @see [ViewPager2.OnPageChangeCallback]
     */
    private fun redrawProgress(
        position: Int,
        positionOffset: Float
    ) {
        for (i in 0 until childCount) {
            val distance =
                (i - position).absoluteValue + if (i <= position) positionOffset else -positionOffset
            val progress = 1 - min(distance, resizingSpan.toFloat()) / resizingSpan.toFloat()
            (getChildAt(i) as Dot).apply {
                this.progress = interpolator.getInterpolation(progress)
                this.color = if (distance < 1) colorEvaluator.evaluate(
                    distance,
                    activeColor,
                    inactiveColor
                ) as Int else inactiveColor
            }
        }
    }

    private fun redrawChildren(count: Int = childCount) {
        removeAllViews()
        for (i in 0 until count) {
            super.addView(Dot(context).apply {
                minSize = dotMinSize
                maxSize = dotMaxSize
                spacing = dotSpacing
                sizeFactor = dotSizeFactor
                setOnClickListener {
                    if (clickEnabled) {
//                        TODO: FIXME
//                        pager?.currentItem = i
                    }
                }
            }, i, generateDefaultLayoutParams())
        }
    }

    private fun resetChildrenAttributes() {
        for (i in 0 until childCount) {
            (getChildAt(i) as Dot).apply {
                minSize = dotMinSize
                maxSize = dotMaxSize
                spacing = dotSpacing
            }
        }
    }


    // Dot section
    private inner class Dot @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        var minSize: Int = 0
            set(value) {
                field = value
                requestLayout()
                invalidate()
            }

        var maxSize: Int = 0
            set(value) {
                field = value
                requestLayout()
                invalidate()
            }

        var spacing: Int = 0
            set(value) {
                field = value
                requestLayout()
                invalidate()
            }

        var progress: Float = 1.0f
            set(value) {
                field = value
                invalidate()
            }

        var color: Int = Color.BLACK
            set(value) {
                field = value
                paint.color = value
                invalidate()
            }

        var sizeFactor: Float = 1.0f
            set(value) {
                field = value
                invalidate()
            }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec((max(minSize, maxSize) * sizeFactor).toInt() + spacing, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((max(minSize, maxSize) * sizeFactor).toInt(), MeasureSpec.EXACTLY)
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawCircle(
                width.toFloat() / 2,
                height.toFloat() / 2,
                ((maxSize - minSize) * progress + minSize) / 2,
                paint
            )
        }
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
