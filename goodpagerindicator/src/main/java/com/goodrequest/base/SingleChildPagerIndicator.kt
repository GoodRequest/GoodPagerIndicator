package com.goodrequest.base

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

abstract class SingleChildPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseGoodPagerIndicator(context, attrs, defStyleAttr) {

    abstract fun onMeasureDot(widthMeasureSpec: Int, heightMeasureSpec: Int): Pair<Int, Int>

    abstract fun onDrawDot(canvas: Canvas, position: Float, itemCount: Int)

    override fun onScroll(itemCount: Int, position: Int, positionOffset: Float) {
        if (childCount != 1) {
            addView(Dot(context).apply {
                drawing = { canvas, position -> onDrawDot(canvas, position, itemCount) }
                measuring = { w, h -> onMeasureDot(w, h) }
            })
        }

        (getChildAt(0) as Dot).apply {
            this.position = abs(positionOffset + position)
            invalidate()
        }
    }

    private inner class Dot @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        var drawing: (Canvas, Float) -> Unit = { _, _ -> }
        var measuring: (widthMeasureSpec: Int, heightMeasureSpec: Int) -> Pair<Int, Int> = { w, h -> w to h }
        var position: Float = 0F

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val (widthSpec, heightSpec) = measuring(widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(widthSpec, heightSpec)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            drawing(canvas, position)
        }
    }
}