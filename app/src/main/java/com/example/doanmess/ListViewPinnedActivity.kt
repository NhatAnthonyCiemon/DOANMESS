package com.example.doanmess

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ListViewPinnedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_view_pinned)

        val listView: ListView = findViewById(R.id.listViewPinned)

        // Dữ liệu mẫu
        val pinnedItems = listOf("Pinned 1", "Pinned 2", "Pinned 3", "Pinned 4", "Pinned 5")

        // Sử dụng ArrayAdapter để hiển thị dữ liệu
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pinnedItems)
        listView.adapter = adapter

        val imgBack = findViewById<ImageView>(R.id.imgBack)
        imgBack.setOnClickListener {
            finish() // Close the current activity and go back to the previous one
        }
    }
}