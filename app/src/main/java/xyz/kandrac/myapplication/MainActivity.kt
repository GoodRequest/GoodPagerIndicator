package xyz.kandrac.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_main.*

private data class AdapterItem(val image: Int, val text: String, var selected: Boolean)

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
