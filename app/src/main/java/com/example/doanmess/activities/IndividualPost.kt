package com.example.doanmess.activities


import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doanmess.models.Post
import com.example.doanmess.adapters.PostAdapter
import com.example.doanmess.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class IndividualPost  : HandleOnlineActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var backBtn: ImageButton
    private lateinit var messageBtn: ImageButton
    private var currentUser = ""
    private var targetUser = ""
    private val postList = mutableListOf<Post>()
    private lateinit var name :TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_individual_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentUser = intent.getStringExtra("current") ?: ""
        targetUser = intent.getStringExtra("target") ?: ""
        val profilePic = intent.getStringExtra("avatar") ?: ""
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postList, { post -> likePost(post) }, { post -> navigateToIndividualPost(post) }, { post -> navigateToIndividualPost(post) })
        recyclerView.adapter = postAdapter
        name = findViewById(R.id.name)
        backBtn = findViewById(R.id.back_button)
        backBtn.setOnClickListener {
            finish()
        }

        messageBtn = findViewById(R.id.message_button)
        messageBtn.setOnClickListener {
            val intent = Intent(this, MainChat::class.java)

            intent.putExtra("uid", targetUser)
            intent.putExtra("name", name.text.toString())
            intent.putExtra("avatar", profilePic)
            startActivity(intent)
        }
        //stop video when scroll
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                    val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? PostAdapter.PostViewHolder
                    viewHolder?.let {
                        if (it.videoPreview.player?.isPlaying == true) {
                            val rect = Rect()
                            it.itemView.getGlobalVisibleRect(rect)
                            val height = it.itemView.height
                            val visibleHeight = rect.height()

                            if (visibleHeight < height / 2) {
                                it.videoPreview.player?.playWhenReady = false
                            }
                        }
                    }
                }
            }
        })
      //  loadPosts()
    }
    override fun onResume() {
        super.onResume()
        Log.d("PostActivity", "onResume")
        postList.clear()
        postAdapter.notifyDataSetChanged()
        loadPosts()
    }
    private fun loadPosts() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(targetUser).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("Name") ?: ""
                    name.text = username
                    val avatar = document.getString("Avatar") ?: ""
                    val liked = document.get("Posts.liked") as? List<String> ?: emptyList()
                    firestore.collection("posts")
                        .whereEqualTo("uid", targetUser)
                        .get()
                        .addOnSuccessListener { postDocs ->
                            val posts = postDocs.map { doc ->
                                Post(
                                    uid = doc.getString("uid") ?: "",
                                    id = doc.id,
                                    profilePic = avatar,
                                    username = username,
                                    time = doc.getLong("time") ?: 0L,
                                    title = doc.getString("title") ?: "",
                                    mediaFile = doc.getString("mediaFile") ?: "",
                                    type = doc.getString("type") ?: "",
                                    likes = (doc.getLong("likes") ?: 0L).toInt(),
                                    liked = liked.contains(doc.id)
                                )
                            }.sortedByDescending { it.time }
                            postList.clear()
                            postList.addAll(posts)
                            postAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                            Log.d("IndividualPost", "Error getting posts: ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("IndividualPost", "Error getting user: ", exception)
            }
    }

    private fun likePost(post: Post) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(currentUser)
        if (post.liked) {
            post.likes--
            userRef.update("Posts.liked", FieldValue.arrayRemove(post.id))
        } else {
            post.likes++
            userRef.update("Posts.liked", FieldValue.arrayUnion(post.id))
        }
        post.liked = !post.liked
        firestore.collection("posts").document(post.id)
            .update("likes", post.likes)
            .addOnSuccessListener {
                Log.d("PostActivity", "Post likes updated successfully!")
            }
            .addOnFailureListener { e ->
                Log.w("PostActivity", "Error updating post likes", e)
            }
    }

    override fun onPause() {
        super.onPause()
        postAdapter.releaseAllPlayers()
    }
    private fun navigateToIndividualPost(post: Post) {
        // do nothing
    }
}