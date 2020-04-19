package com.goodrequest.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

private data class AdapterItem(val image: Int, val text: String, var selected: Boolean)

private open class SimpleSeekBarListener : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
}

fun SeekBar.onProgress(progressListener: (Int) -> Unit) {
    setOnSeekBarChangeListener(object: SimpleSeekBarListener() {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            progressListener(progress)
        }
    })
}

class MainActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_main)
        pager.adapter = Adapter(adapterItems)
        indicator.initWith(pager)

        minSizeSeekBar.onProgress { indicator.dotMinSize = ((it + 1) * resources.displayMetrics.density).toInt() }
        maxSizeSeekBar.onProgress { indicator.dotMaxSize = ((it + 4)  * resources.displayMetrics.density).toInt()}
        dotSpacingSeekBar.onProgress { indicator.dotSpacing = (it  * resources.displayMetrics.density).toInt() }
        dotSpanSizeSeekBar.onProgress { indicator.resizingSpan = ((it + 1) * resources.displayMetrics.density).toInt() }
        interpolatorLinear.setOnClickListener { indicator.interpolator = LinearInterpolator() }
        interpolatorAccelerate.setOnClickListener { indicator.interpolator = AccelerateInterpolator() }
        interpolatorDecelerate.setOnClickListener { indicator.interpolator = DecelerateInterpolator() }
        interpolatorBounce.setOnClickListener { indicator.interpolator = BounceInterpolator() }
        interpolatorOvershoot.setOnClickListener { indicator.interpolator = OvershootInterpolator() }
        swipeEnabled.setOnClickListener { indicator.swipeEnabled = !indicator.swipeEnabled }
        clickEnabled.setOnClickListener { indicator.clickEnabled = !indicator.clickEnabled }
    }

    private inner class Adapter(val adapterItems: Array<AdapterItem>) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.pager_item, parent, false)
            )

        override fun getItemCount() = adapterItems.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = adapterItems[position]
            holder.image.setImageResource(item.image)
            holder.image.isSelected = item.selected
            holder.text.text = item.text
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val image: ImageView = itemView.findViewById(R.id.item_image)
            val text: TextView = itemView.findViewById(R.id.item_name)
        }
    }
}
