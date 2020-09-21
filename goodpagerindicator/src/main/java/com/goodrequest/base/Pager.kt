package com.goodrequest.base

interface Pager {
    fun beginFakeDrag()
    fun endFakeDrag()
    fun fakeDragBy(offsetPxFloat: Float)
    fun width(): Int
    fun itemCount(): Int
    fun currentItem(item: Int)
    fun registerPageChangeCallback(pageChangeCallback: BaseGoodPagerIndicator.PageChangeCallback)
    fun unregisterPageChangeCallback(pageChangeCallback: BaseGoodPagerIndicator.PageChangeCallback)
    fun registerAdapterDataObserver()
    fun unregisterAdapterDataObserver()
}