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

    abstract fun onDrawDot(canvas: Canvas, position: Int)

    private fun getDotAt(position: Int) = getChildAt(position) as Dot

    override fun onScroll(itemCount: Int, position: Int, positionOffset: Float) {
        if (itemCount != childCount) {
            removeAllViews()
            for (i in 0 until itemCount) {
                addView(Dot(context).apply {
                    drawing = { canvas -> onDrawDot(canvas, i) }
                    measuring = { w, h -> onMeasureDot(w, h) }
                    setOnClickListener { handleClick(i) }
                })
            }
        }

        for (i in 0 until itemCount) {
            getDotAt(i).invalidate()
        }
    }

    private inner class Dot @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        var drawing: (Canvas) -> Unit = { }
        var measuring: (widthMeasureSpec: Int, heightMeasureSpec: Int) -> Pair<Int, Int> = { w, h -> w to h }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val (widthSpec, heightSpec) = measuring(widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(widthSpec, heightSpec)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            drawing(canvas)
        }
    }
}