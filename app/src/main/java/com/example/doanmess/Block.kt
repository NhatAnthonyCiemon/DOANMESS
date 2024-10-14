package com.example.doanmess

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Block : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlockAdapter

    // Dữ liệu giả lập
    private val blockLists = mutableListOf(
        BlockModel("Người lạ 1", "2 hours ago"),
        BlockModel("Người lạ 2", "3 hours ago"),
        BlockModel("Người lạ 4", "5 hours ago"),
        BlockModel("Người lạ 4", "3 hours ago"),
        BlockModel("Người lạ 5", "3 hours ago"),
        BlockModel("Người lạ 6", "2 hours ago"),
        BlockModel("Người lạ 7", "3 hours ago"),
        BlockModel("Người lạ 8", "5 hours ago"),
        BlockModel("Người lạ 9", "3 hours ago"),
        BlockModel("Người lạ 10", "3 hours ago")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_block)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BlockAdapter(blockLists)
        recyclerView.adapter = adapter
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }
}