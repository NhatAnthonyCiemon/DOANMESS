package com.example.doanmess

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FriendRequest : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendRequestAdapter

    // Dữ liệu giả lập
    private val friendRequests = mutableListOf(
        FriendRequestModel("Người lạ 1", "2 hours ago"),
        FriendRequestModel("Người lạ 2", "3 hours ago"),
        FriendRequestModel("Người lạ 4", "5 hours ago"),
        FriendRequestModel("Người lạ 4", "3 hours ago"),
        FriendRequestModel("Người lạ 5", "3 hours ago"),
        FriendRequestModel("Người lạ 6", "2 hours ago"),
        FriendRequestModel("Người lạ 7", "3 hours ago"),
        FriendRequestModel("Người lạ 8", "5 hours ago"),
        FriendRequestModel("Người lạ 9", "3 hours ago"),
        FriendRequestModel("Người lạ 10", "3 hours ago")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_request)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FriendRequestAdapter(friendRequests)
        recyclerView.adapter = adapter
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }
}