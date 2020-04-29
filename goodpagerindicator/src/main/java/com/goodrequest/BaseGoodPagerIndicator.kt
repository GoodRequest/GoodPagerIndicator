package com.goodrequest

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

private const val previewPosition = 3
private const val previewOffset = 0f
private const val previewItemCount = 6

abstract class BaseGoodPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var pager: ViewPager2? = null
    private var dataObserver: AdapterDataObserver? = null
    private var pageChangeCallback: PageChangeCallback? = null

    private val detector: GestureDetector
    private val gesturesCallback = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?) = true

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) =
            pager?.let {
                it.beginFakeDrag()
                it.fakeDragBy(1f) // TODO: fix-me
                true
            } ?: false

    }

    var swipeEnabled: Boolean = true
    var clickEnabled: Boolean = true

    private var lastPosition = previewPosition      // last known "selected item" position
    private var lastPositionOffset = previewOffset  // last known "selected item" offset position

    init {
        orientation = HORIZONTAL

        detector = GestureDetector(context, gesturesCallback)
    }

    fun initWith(pager: ViewPager2) {
        this.pager = pager
    }

    fun redraw() {
        onScroll(childCount, lastPosition, lastPositionOffset)
    }

    abstract fun onScroll(itemCount: Int, position: Int, positionOffset: Float)

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
            removeAllViews()
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
        if (isInEditMode) {
            onScroll(previewItemCount, previewPosition, previewOffset)
        }
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

    // override unsupported functions
//    override fun addView(child: View?) = throw UnsupportedOperationException("addView calls are not supported for indicator")
//    override fun addView(child: View?, params: ViewGroup.LayoutParams?) = throw UnsupportedOperationException("addView calls are not supported for indicator")
//    override fun addView(child: View?, index: Int) = throw UnsupportedOperationException("addView calls are not supported for indicator")
//    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) = throw UnsupportedOperationException("addView calls are not supported for indicator")
//    override fun addView(child: View?, width: Int, height: Int) = throw UnsupportedOperationException("addView calls are not supported for indicator")
//    override fun addViewInLayout(child: View?, index: Int, params: ViewGroup.LayoutParams?) = throw UnsupportedOperationException("addViewInLayout calls are not supported for indicator")
//    override fun addViewInLayout(child: View?, index: Int, params: ViewGroup.LayoutParams?, preventRequestLayout: Boolean) = throw UnsupportedOperationException("addViewInLayout calls are not supported for indicator")

}