package com.goodrequest.base

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

abstract class SameChildCountPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseGoodPagerIndicator(context, attrs, defStyleAttr) {

    abstract fun onMeasureDot(widthMeasureSpec: Int, heightMeasureSpec: Int): Pair<Int, Int>

    /**
     * [distanceFactor] respresents how far away is current page focus. 0 means the dot
     * is fully focused on. 10 means, focus is on 10.th dot from this one
     */
    abstract fun onDrawDot(canvas: Canvas, distanceFactor: Float)

    private fun getDotAt(position: Int) = getChildAt(position) as Dot

    override fun onScroll(itemCount: Int, position: Int, positionOffset: Float) {
        if (itemCount != childCount) {
            removeAllViews()
            for (i in 0 until itemCount) {
                addView(Dot(context).apply {
                    drawing = { canvas, distanceFactor -> onDrawDot(canvas, distanceFactor) }
                    measuring = { w, h -> onMeasureDot(w, h) }
                    setOnClickListener { handleClick(i) }
                })
            }
        }

        for (i in 0 until itemCount) {
            getDotAt(i).apply {
                distanceFactor = abs(positionOffset + position - i)
                invalidate()
            }
        }
    }
}

class Dot @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var drawing: (Canvas, Float) -> Unit = { _, _ ->  }
    var measuring: (widthMeasureSpec: Int, heightMeasureSpec: Int) -> Pair<Int, Int> = { w, h -> w to h }
    var distanceFactor: Float = 0F

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (widthSpec, heightSpec) = measuring(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthSpec, heightSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawing(canvas, distanceFactor)
    }
}