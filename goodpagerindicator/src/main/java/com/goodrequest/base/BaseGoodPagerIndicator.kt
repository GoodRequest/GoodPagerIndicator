package com.goodrequest.base

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.goodrequest.R
import kotlin.math.abs

private const val previewPosition = 3
private const val previewOffset = 0f
private const val previewItemCount = 6

@Suppress("LeakingThis")
abstract class BaseGoodPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Pager related fields
    private var pager: Pager? = null
    private var dataObserver: AdapterDataObserver? = null
    private var pageChangeCallback: PageChangeCallback? = null

    // Position related fields
    private var lastKnownPosition = previewPosition // last known "selected item" position
    private var lastKnownOffset = previewOffset     // last known "selected item" offset position

    // Gesture handling
    private val detector: GestureDetector
    private val gesturesCallback = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?) = true

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
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

    fun initWith(pager: ViewPager) {
        this.pager = PagerAdapter(pager)
        redraw()
    }

    fun initWith(pager: ViewPager2) {
        this.pager = Pager2Adapter(pager)
        redraw()
    }

    fun redraw() {
        removeAllViews()
        onScroll()
    }

    fun handleClick(position: Int) {
        if (clickEnabled) {
            pager?.currentItem(position)
        }
    }

    /**
     * Scroll event occurs. All necessary values can be found using these getters:
     *
     * - [getRelativeDistance]
     * - [position]
     * - [positionOffset]
     * - [absolutePosition]
     * - [itemCount]
     * - [progress]
     */
    abstract fun onScroll()

    /**
     *
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getRelativeDistance(position: Int) = abs(absolutePosition - position)

    /**
     * Currently active item position. This position is equivalent of `position` obtained from
     * [PageChangeCallback.onPageScrolled], so if scroll is anywhere between 2 pages, the
     * `position` will always point on first page (if total scroll is 1.9, the `position` will
     * still be 1)
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val position get() = lastKnownPosition

    /**
     * Current offset between 2 pages as a percentage. In between progress.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val positionOffset get() = lastKnownOffset

    /**
     * Absolute position of pager indicator. If indicator is idle, the value will be whole number
     * matching page number - 1. If indicator is between f.e. 3rd and 4th position, its value
     * will be somewhere in interval (2, 3)
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val absolutePosition get() = lastKnownOffset + lastKnownPosition

    @Suppress("MemberVisibilityCanBePrivate")
    val itemCount get() = if (isInEditMode) previewItemCount else pager?.itemCount() ?: 0

    /**
     * Completion progress of pager indicator. If you are scrolled on last item, the value
     * will be 1.0, if you are scrolled on first item, value will be 0.0.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val progress get() = when (itemCount) {
        0 -> 0F
        1 -> 1F
        else -> (lastKnownOffset + lastKnownPosition) / (itemCount - 1)
    }

    /**
     * Here you can handle swipe gestures over whole pager indicator. Just call
     * [ViewPager2.fakeDragBy] method with computed scroll distance. You will most
     * probably need to operate with [ViewPager2.getWidth] and the pager indicator
     * children widths / counts
     */
    open fun computeSwipe(
        pager: Pager,
        distanceX: Float,
        distanceY: Float
    ) {
        val childrenWidth = (getChildAt(childCount - 1).right - getChildAt(0).left)
        val pagerWidth = pager.width()
        pager.fakeDragBy(distanceX * pagerWidth / (childrenWidth / childCount))
    }

    inner class PageChangeCallback : ViewPager2.OnPageChangeCallback(), ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            lastKnownPosition = position
            lastKnownOffset = positionOffset
            onScroll()
        }
    }

    private inner class AdapterDataObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            redraw()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.pager?.registerPageChangeCallback(PageChangeCallback().also {
            pageChangeCallback = it
        })
        this.pager?.registerAdapterDataObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pageChangeCallback?.let {
            this.pager?.unregisterPageChangeCallback(it)
        }
        dataObserver?.let {
            this.pager?.unregisterAdapterDataObserver()
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

    inner class ViewPagerDataObserver : DataObserver, DataSetObserver() {
        override fun onChanged() = redraw()
    }

    inner class PagerAdapter(private val viewPager: ViewPager) :
        Pager {

        private val dataObserver = ViewPagerDataObserver()
        private var registered = false
        private var fakeDragging = false

        override fun beginFakeDrag() {
            fakeDragging = true
            viewPager.beginFakeDrag()
        }

        override fun endFakeDrag() {
            if (!fakeDragging) return
            viewPager.endFakeDrag()
            fakeDragging = false
        }

        override fun fakeDragBy(offsetPxFloat: Float) {
            viewPager.fakeDragBy(offsetPxFloat)
        }

        override fun width(): Int {
            return viewPager.width
        }

        override fun itemCount(): Int {
            return viewPager.adapter?.count ?: 0
        }

        override fun currentItem(item: Int) {
            viewPager.currentItem = item
        }

        override fun registerPageChangeCallback(pageChangeCallback: PageChangeCallback) {
            viewPager.addOnPageChangeListener(pageChangeCallback)
        }

        override fun unregisterPageChangeCallback(pageChangeCallback: PageChangeCallback) {
            viewPager.removeOnPageChangeListener(pageChangeCallback)
        }

        override fun registerAdapterDataObserver() {
            if (registered) return
            viewPager.adapter?.let {
                it.registerDataSetObserver(dataObserver)
                registered = true
            }
        }

        override fun unregisterAdapterDataObserver() {
            if (!registered) return
            viewPager.adapter?.let {
                it.unregisterDataSetObserver(dataObserver)
                registered = false
            }
        }
    }

    inner class Pager2Adapter(private val viewPager: ViewPager2) :
        Pager {

        private val dataObserver = ViewPager2DataObserver()
        private var registered = false
        private var fakeDragging = false

        override fun beginFakeDrag() {
            fakeDragging = true
            viewPager.beginFakeDrag()
        }

        override fun endFakeDrag() {
            if (!fakeDragging) return
            viewPager.endFakeDrag()
            fakeDragging = false
        }

        override fun fakeDragBy(offsetPxFloat: Float) {
            viewPager.fakeDragBy(offsetPxFloat)
        }

        override fun width(): Int {
            return viewPager.width
        }

        override fun itemCount(): Int {
            return viewPager.adapter?.itemCount ?: 0
        }

        override fun currentItem(item: Int) {
            viewPager.currentItem = item
        }

        override fun registerPageChangeCallback(pageChangeCallback: PageChangeCallback) {
            viewPager.registerOnPageChangeCallback(pageChangeCallback)
        }

        override fun unregisterPageChangeCallback(pageChangeCallback: PageChangeCallback) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        }

        override fun registerAdapterDataObserver() {
            if (registered) return
            viewPager.adapter?.let {
                it.registerAdapterDataObserver(dataObserver)
                registered = true
            }
        }

        override fun unregisterAdapterDataObserver() {
            if (!registered) return
            viewPager.adapter?.let {
                it.unregisterAdapterDataObserver(dataObserver)
                registered = false
            }
        }
    }

    inner class ViewPager2DataObserver : DataObserver, RecyclerView.AdapterDataObserver() {
        override fun onChanged() = redraw()
    }

    interface DataObserver {
        fun onChanged()
    }

}