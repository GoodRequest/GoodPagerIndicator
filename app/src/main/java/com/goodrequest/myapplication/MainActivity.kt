package com.goodrequest.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewpagerButton.setOnClickListener {
            startActivity(Intent(this, ViewPagerActivity::class.java))
        }
        viewpager2Button.setOnClickListener {
            startActivity(Intent(this, ViewPager2Activity::class.java))
        }
    }
}
