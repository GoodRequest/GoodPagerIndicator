package com.goodrequest.base

import android.content.Context
import android.util.AttributeSet
import android.view.View

abstract class SingleChildViewPagerIndicator<T: View> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseGoodPagerIndicator(context, attrs, defStyleAttr) {

    abstract fun createView(context: Context) : T

    /**
     * Called after [onScroll] without any notification to [invalidate] the view.
     * If you need to invalidate the view, you can simply call `view.invalidate()`
     */
    open fun editOnScroll(view: T) {}

    override fun onScroll(itemCount: Int, position: Int, positionOffset: Float) {
        @Suppress("UNCHECKED_CAST")
        val child = getChildAt(0) as T? ?: createView(context).apply { addView(this) }
        editOnScroll(child)
    }
}