package com.goodrequest.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_pager.*
import kotlinx.android.synthetic.main.pager_item.view.*

class ViewPagerActivity : AppCompatActivity() {

    private val adapterItems = arrayOf(
        AdapterItem(R.drawable.ic_launcher_background, "Study", false),
        AdapterItem(R.drawable.ic_launcher_background, "Children", false),
        AdapterItem(R.drawable.ic_launcher_background, "Cars", false),
        AdapterItem(R.drawable.ic_launcher_background, "Travel", false),
        AdapterItem(R.drawable.ic_launcher_background, "Pension", false),
        AdapterItem(R.drawable.ic_launcher_background, "Food", false),
        AdapterItem(R.drawable.ic_launcher_background, "Hotel", false),
        AdapterItem(R.drawable.ic_launcher_background, "Investments", false)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        pager.adapter = Adapter(adapterItems)
        indicator1.initWith(pager)
        indicator2.initWith(pager)
        indicator3.initWith(pager)
        indicator4.initWith(pager)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private class Adapter(val items: Array<AdapterItem>) :
        androidx.viewpager.widget.PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val item = items[position]
            val inflater = LayoutInflater.from(container.context)
            val layout = LayoutInflater.from(container.context)
                .inflate(R.layout.pager_item, container, false)
            layout.item_name.text = item.text
            layout.item_image.setImageResource(item.image)
            container.addView(layout)
            return layout
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }
    }
}
