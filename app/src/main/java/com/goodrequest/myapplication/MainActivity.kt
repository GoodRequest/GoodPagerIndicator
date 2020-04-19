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
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

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

        // set seek-bars to match indicator internal values
        minSizeSeekBar.progress = (indicator.dotMinSize / resources.displayMetrics.density).roundToInt()
        maxSizeSeekBar.progress = (indicator.dotMaxSize / resources.displayMetrics.density).roundToInt() - 4
        dotSpacingSeekBar.progress = (indicator.dotSpacing / resources.displayMetrics.density).roundToInt()
        dotSpanSizeSeekBar.progress = indicator.resizingSpan - 1

        minSizeSeekBar.onProgress { indicator.dotMinSize = (it * resources.displayMetrics.density).toInt() }
        maxSizeSeekBar.onProgress { indicator.dotMaxSize = ((it + 4)  * resources.displayMetrics.density).toInt()}
        dotSpacingSeekBar.onProgress { indicator.dotSpacing = (it  * resources.displayMetrics.density).toInt() }
        dotSpanSizeSeekBar.onProgress { indicator.resizingSpan = it + 1 }

        interpolatorLinear.setOnClickListener {
            indicator.interpolator = LinearInterpolator()
            Toast.makeText(this, "LinearInterpolator selected", Toast.LENGTH_SHORT).show()
        }
        interpolatorAccelerate.setOnClickListener {
            indicator.interpolator = AccelerateInterpolator()
            Toast.makeText(this, "AccelerateInterpolator selected", Toast.LENGTH_SHORT).show()
        }
        interpolatorDecelerate.setOnClickListener {
            indicator.interpolator = DecelerateInterpolator()
            Toast.makeText(this, "DecelerateInterpolator selected", Toast.LENGTH_SHORT).show()
        }
        interpolatorBounce.setOnClickListener {
            indicator.interpolator = BounceInterpolator()
            Toast.makeText(this, "BounceInterpolator selected", Toast.LENGTH_SHORT).show()
        }
        interpolatorOvershoot.setOnClickListener {
            indicator.interpolator = OvershootInterpolator()
            Toast.makeText(this, "OvershootInterpolator selected", Toast.LENGTH_SHORT).show()
        }
        swipeEnabled.setOnClickListener {
            indicator.swipeEnabled = !indicator.swipeEnabled
            Toast.makeText(this, "swipe ${if (indicator.swipeEnabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
        clickEnabled.setOnClickListener {
            indicator.clickEnabled = !indicator.clickEnabled
            Toast.makeText(this, "click ${if (indicator.clickEnabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
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
