package com.goodrequest

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

private const val previewPosition = 3
private const val previewOffset = 0f
private const val previewItemCount = 6

@Suppress("LeakingThis")
abstract class BaseGoodPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Pager related fields
    private var pager: ViewPager2? = null
    private var dataObserver: AdapterDataObserver? = null
    private var pageChangeCallback: PageChangeCallback? = null

    // Position related fields
    private var lastPosition = previewPosition      // last known "selected item" position
    private var lastPositionOffset = previewOffset  // last known "selected item" offset position

    // Gesture handling
    private val detector: GestureDetector
    private val gesturesCallback = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?) = true

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) : Boolean {
            return pager?.let {
                it.beginFakeDrag()
                computeSwipe(it, distanceX, distanceY)
                true
            } ?: false
        }
    }

    // Behavior restrictions
    var swipeEnabled: Boolean = true
    var clickEnabled: Boolean = true

    init {
        orientation = HORIZONTAL
        detector = GestureDetector(context, gesturesCallback)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GoodPagerIndicator,
            0, 0
        ).apply {
            swipeEnabled = getBoolean(R.styleable.BaseGoodPagerIndicator_indicator_swipe_enabled, true)
            clickEnabled = getBoolean(R.styleable.BaseGoodPagerIndicator_indicator_click_enabled, true)
        }
        redraw()
    }

    fun initWith(pager: ViewPager2) {
        this.pager = pager
        redraw()
    }

    fun redraw() {
        removeAllViews()
        if (isInEditMode) {
            onScroll(previewItemCount, previewPosition, previewOffset)
        } else {
            onScroll(pager?.adapter?.itemCount ?: 0, lastPosition, lastPositionOffset)
        }
    }

    fun handleClick(position: Int) {
        if (clickEnabled) {
            pager?.currentItem = position
        }
    }

    abstract fun onScroll(itemCount: Int, position: Int, positionOffset: Float)

    fun getLastPosition() = lastPosition
    fun getLastPositionOffset() = lastPositionOffset

    /**
     * Here you can handle swipe gestures over whole pager indicator. Just call
     * [ViewPager2.fakeDragBy] method with computed scroll distance. You will most
     * probably need to operate with [ViewPager2.getWidth] and the pager indicator
     * children widths / counts
     */
    open fun computeSwipe(
        pager: ViewPager2,
        distanceX: Float,
        distanceY: Float
    ) {
        val childrenWidth = (getChildAt(childCount - 1).right - getChildAt(0).left)
        val pagerWidth = pager.width
        pager.fakeDragBy(distanceX * pagerWidth / (childrenWidth / childCount))
    }

    // ViewPager2 listeners section
    private inner class PageChangeCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            lastPosition = position
            lastPositionOffset = positionOffset
            onScroll(pager?.adapter?.itemCount ?: 0, position, positionOffset)
        }
    }

    private inner class AdapterDataObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            redraw()
        }
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
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (swipeEnabled) {
            detector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                pager?.endFakeDrag()
            }
        }
        return false
    }
}