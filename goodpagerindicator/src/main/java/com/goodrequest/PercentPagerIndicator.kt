package com.goodrequest

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.goodrequest.base.SingleChildViewPagerIndicator

@Suppress("unused")
class PercentPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SingleChildViewPagerIndicator<TextView>(context, attrs, defStyleAttr) {

    override fun createView(context: Context): TextView = TextView(context)

    override fun editOnScroll(view: TextView) {
        view.text = String.format("%.0f%%", progress * 100)
    }

}