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

        // Lấy dữ liệu từ Intent
        val pinnedMessages = intent.extras?.getSerializable("pinnedMessages") as? ArrayList<Map<String, String>>

        // Gán ListView và hiển thị dữ liệu
        val listView: ListView = findViewById(R.id.listViewPinned)

        // Biến đổi danh sách các tin nhắn thành chuỗi có thể hiển thị
        val messagesList = pinnedMessages?.map { message ->
            val content = message["Content"] ?: "Nội dung không xác định"
            val name = message["Name"] ?: "Tên không xác định"
            "$name: $content"
        } ?: listOf("Không có tin nhắn nào")

        // Sử dụng ArrayAdapter để hiển thị tin nhắn trong ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messagesList)
        listView.adapter = adapter

        val imgBack = findViewById<ImageView>(R.id.imgBack)
        imgBack.setOnClickListener {
            finish() // Close the current activity and go back to the previous one
        }

    }
}


