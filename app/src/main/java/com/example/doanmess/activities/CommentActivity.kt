package com.example.doanmess.activities

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doanmess.R
import com.example.doanmess.adapters.CommentAdapter
import com.example.doanmess.models.Comment
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.ArrayList

class CommentActivity : AppCompatActivity() {
    private lateinit var currentUser: String
    private lateinit var adapter: CommentAdapter
    private val comments = mutableListOf<Comment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
        // Retrieve post details from intent
        val postId = intent.getStringExtra("postId")
        val postComments = intent.getIntExtra("postComments",0)
        currentUser = intent.getStringExtra("current") ?: ""
        // Use the post details to display comments
        val commentInput = findViewById<EditText>(R.id.commentInput)
        val postCommentButton = findViewById<ImageButton>(R.id.postCommentButton)
        postCommentButton.setOnClickListener {
            val comment = commentInput.text.toString()
            if (comment.isNotEmpty()) {
                val firestore = FirebaseFirestore.getInstance()
                val commentData = hashMapOf(
                    "postId" to postId,
                    "uid" to currentUser,
                    "likes" to arrayListOf<String>(),
                    "time" to System.currentTimeMillis(),
                    "content" to comment
                )
                firestore.collection("comments").add(commentData)
                    .addOnSuccessListener {
                        // Update the comments count in the Posts collection
                        firestore.collection("posts").document(postId!!)
                            .update("comments", FieldValue.increment(1))
                            .addOnSuccessListener {
                                Log.d("CommentActivity", "Comment added and post comments count updated successfully!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("CommentActivity", "Error updating post comments count", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.w("CommentActivity", "Error adding comment", e)
                    }
                // Clear the input field
                commentInput.text.clear()
                loadComments(postId!!)
            }
            else{
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }

        val recyclerViewComments = findViewById<RecyclerView>(R.id.commentsRecyclerView)
        recyclerViewComments.layoutManager = LinearLayoutManager(this)


        adapter = CommentAdapter(comments, ::onLikeClick, ::onUserImageClick)
        recyclerViewComments.adapter = adapter
        loadComments(postId!!)
        val data = Intent().apply {
            putExtra("postId", postId)
        }
        setResult(Activity.RESULT_OK, data)
    }
    private fun onLikeClick(comment: Comment) {
        // Handle like button click
        comment.liked = !comment.liked
        val firestore = FirebaseFirestore.getInstance()
        val commentRef = firestore.collection("comments").document(comment.id)
        if (!comment.liked) {
            comment.likes.remove(currentUser)
            commentRef.update("likes", FieldValue.arrayRemove(currentUser))
        } else {
            comment.likes.add(currentUser)
            commentRef.update("likes", FieldValue.arrayUnion(currentUser))
        }
    }

    private fun onUserImageClick(comment: Comment) {
        // Handle user image click
        val intent = Intent(this, IndividualPost::class.java)
        intent.putExtra("current", currentUser)
        intent.putExtra("target",  comment.uid)
        intent.putExtra("avatar", comment.profilePic)
        intent.putExtra("username", comment.username)
        startActivity(intent)
    }
    override fun finish() {
        setResult(RESULT_OK, intent)
        super.finish()
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out_down)
    }
    private fun loadComments(postId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val comments = mutableListOf<Comment>()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val commentDocuments = firestore.collection("comments")
                    .whereEqualTo("postId", postId)
                    .get()
                    .await()

                val userIds = commentDocuments.mapNotNull { it.getString("uid") }.toSet()
                val userDocuments = firestore.collection("users")
                    .whereIn(FieldPath.documentId(), userIds.toList())
                    .get()
                    .await()
                val userMap = userDocuments.associateBy({ it.id }, {
                    Pair(it.getString("Avatar"), it.getString("Name"))
                })
                commentDocuments.forEach { document ->
                    val uid = document.getString("uid")
                    val time = document.getLong("time")
                    val content = document.getString("content")
                    val likes = document.get("likes") as? ArrayList<String> ?: arrayListOf()
                    val userInfo = userMap[uid]

                    if (uid != null && time != null && content != null && userInfo != null) {
                        val comment = Comment(
                            uid = uid,
                            id = document.id,
                            profilePic = userInfo.first ?: "",
                            username = userInfo.second ?: "",
                            time = time,
                            likes = likes,
                            liked = likes.contains(currentUser),
                            content = content
                        )
                        comments.add(comment)
                    }
                }
                withContext(Dispatchers.Main) {
                    comments.sortByDescending { it.time }
                    adapter = CommentAdapter(comments, ::onLikeClick, ::onUserImageClick)
                    findViewById<RecyclerView>(R.id.commentsRecyclerView).adapter = adapter
                }
            } catch (e: Exception) {
                Log.w("CommentActivity", "Error getting comments or users: ", e)
            }
        }
    }
}