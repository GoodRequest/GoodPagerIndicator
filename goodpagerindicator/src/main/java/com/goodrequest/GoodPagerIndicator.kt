package com.goodrequest

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.absoluteValue
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

private const val previewPosition = 3
private const val previewOffset = 0f
private const val previewItemCount = 6

@Suppress("MemberVisibilityCanBePrivate", "unused")
class GoodPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var pager: ViewPager2? = null
    private var dataObserver: AdapterDataObserver? = null
    private var pageChangeCallback: PageChangeCallback? = null

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
            invalidate()
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
            invalidate()
        }

    private val colorEvaluator = ArgbEvaluator()
    private val detector: GestureDetector
    private val gesturesCallback = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?) = true

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) =
            pager?.let {
                it.beginFakeDrag()
                it.fakeDragBy(distanceX / (dotSpacing + dotMaxSize) * (pager?.width ?: 0))
                true
            } ?: false

    }

    init {
        orientation = HORIZONTAL

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
            } finally {
                recycle()
            }
        }

        detector = GestureDetector(context, gesturesCallback)

        if (isInEditMode) {
            redrawProgress(previewPosition, previewOffset)
        }
    }

    fun initWith(pager: ViewPager2) {
        this.pager = pager
        redrawChildren()
    }

    // computation section
    private fun redrawProgress(position: Int, positionOffset: Float) {
        redrawChildren()
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

    private fun redrawChildren() {
        if (pager?.adapter?.itemCount != childCount || isInEditMode) {
            removeAllViews()
            for (i in 0 until (pager?.adapter?.itemCount
                ?: if (isInEditMode) previewItemCount else 0)) {
                super.addView(Dot(context).apply {
                    minSize = dotMinSize
                    maxSize = dotMaxSize
                    spacing = dotSpacing
                    setOnClickListener {
                        pager?.currentItem = i
                    }
                }, i, generateDefaultLayoutParams())
            }
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

    // ViewPager2 listeners section
    private inner class PageChangeCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) = redrawProgress(position, positionOffset)
    }

    private inner class AdapterDataObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() = redrawChildren()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.pager?.registerOnPageChangeCallback(PageChangeCallback().also {
            pageChangeCallback = it
        })
        this.pager?.adapter?.registerAdapterDataObserver(AdapterDataObserver().also {
            dataObserver = it
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pageChangeCallback?.let {
            this.pager?.unregisterOnPageChangeCallback(it)
        }
        dataObserver?.let {
            this.pager?.adapter?.unregisterAdapterDataObserver(it)
        }
    }

    // Gestures section
    override fun onInterceptTouchEvent(ev: MotionEvent) = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        //
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            pager?.endFakeDrag()
            return true
        }
        return detector.onTouchEvent(event)
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

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(maxSize + spacing, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY)
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

    // override unsupported functions

    override fun addView(child: View?) = throw UnsupportedOperationException("addView calls are not supported for indicator")
    override fun addView(child: View?, params: ViewGroup.LayoutParams?) = throw UnsupportedOperationException("addView calls are not supported for indicator")
    override fun addView(child: View?, index: Int) = throw UnsupportedOperationException("addView calls are not supported for indicator")
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) = throw UnsupportedOperationException("addView calls are not supported for indicator")
    override fun addView(child: View?, width: Int, height: Int) = throw UnsupportedOperationException("addView calls are not supported for indicator")
    override fun addViewInLayout(child: View?, index: Int, params: ViewGroup.LayoutParams?) = throw UnsupportedOperationException("addViewInLayout calls are not supported for indicator")
    override fun addViewInLayout(child: View?, index: Int, params: ViewGroup.LayoutParams?, preventRequestLayout: Boolean) = throw UnsupportedOperationException("addViewInLayout calls are not supported for indicator")

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
