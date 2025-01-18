package com.example.doanmess.activities

import android.app.ActivityOptions
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.doanmess.R

class CommentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Retrieve post details from intent
        val postId = intent.getStringExtra("postId")
        val postTitle = intent.getStringExtra("postTitle")
        val postMediaFile = intent.getStringExtra("postMediaFile")
        val postType = intent.getStringExtra("postType")
        val postLikes = intent.getIntExtra("postLikes", 0)
        val postLiked = intent.getBooleanExtra("postLiked", false)

        // Use the post details to display comments

    }
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out_down)
    }
}