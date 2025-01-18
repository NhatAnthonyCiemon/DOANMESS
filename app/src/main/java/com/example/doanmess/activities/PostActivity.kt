package com.example.doanmess.activities

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostActivity : HandleOnlineActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var backBtn : ImageButton
    private var currentUser = ""
    private val postList = mutableListOf<Post>()
    private var posts = listOf<Post>()
    private lateinit var doanmessText : TextView
    private fun showCommentSection(post: Post) {
        val intent = Intent(this, CommentActivity::class.java).apply {
            putExtra("postId", post.id)
            putExtra("postTitle", post.title)
            putExtra("postMediaFile", post.mediaFile)
            putExtra("postType", post.type)
            putExtra("postLikes", post.likes)
            putExtra("postLiked", post.liked)
        }
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_up, R.anim.no_anim)
        startActivity(intent, options.toBundle())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if(currentUser.isEmpty()) {
            finish()
            return
        }
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postList, { post -> likePost(post) }, { post -> navigateToIndividualPost(post) }, { post -> showCommentSection(post) })
        recyclerView.adapter = postAdapter

        findViewById<ImageButton>(R.id.addPost).setOnClickListener {
            val intent = Intent(this, NewPost::class.java)
            intent.putExtra("uid", currentUser)
            startActivity(intent)
        }
        backBtn = findViewById(R.id.back_button)
        backBtn.setOnClickListener {
            finish()
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
        doanmessText = findViewById(R.id.doanmessText)
        doanmessText.setOnClickListener {
            recyclerView.smoothScrollToPosition(0)
            reloadPosts()
        }
        loadPosts()
    }
    override fun onResume() {
        super.onResume()
        Log.d("PostActivity", "onResume")
    }
    fun reloadPosts() {
        postList.clear()
        postAdapter.notifyDataSetChanged()
        loadPosts()
    }
    override fun onPause() {
        super.onPause()
        postAdapter.releaseAllPlayers()
    }
    private fun loadPosts() {
        val firestore = FirebaseFirestore.getInstance()
        val friendsRef = firestore.collection("users").document(currentUser)
        friendsRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val friends = document.get("Friends") as? List<String> ?: emptyList()
                val liked = document.get("Posts.liked") as? List<String> ?: emptyList()
                val userIds = friends + currentUser
                val userMap = mutableMapOf<String, Pair<String, String>>()

                firestore.collection("users")
                    .whereIn(FieldPath.documentId(), userIds)
                    .get()
                    .addOnSuccessListener { userDocs ->
                        userDocs.forEach { doc ->
                            val uid = doc.id
                            val userName = doc.getString("Name") ?: ""
                            val profilePic = doc.getString("Avatar") ?: ""
                            userMap[uid] = Pair(userName, profilePic)
                        }
                        firestore.collection("posts")
                            .whereIn("uid", userIds)
                            .get()
                            .addOnSuccessListener { postDocs ->
                                posts = postDocs.map { doc ->
                                    val uid = doc.getString("uid") ?: ""
                                    val userInfo = userMap[uid] ?: Pair("", "")
                                    Post(
                                        uid = uid,
                                        id = doc.id,
                                        profilePic = userInfo.second,
                                        username = userInfo.first,
                                        time = doc.getLong("time") ?: 0L,
                                        title = doc.getString("title") ?: "",
                                        mediaFile = doc.getString("mediaFile") ?: "",
                                        type = doc.getString("type") ?: "",
                                        likes = (doc.getLong("likes") ?: 0L).toInt(),
                                        liked = liked.contains(doc.id),
                                        comments = (doc.getLong("comments") ?: 0L).toInt()
                                    )
                                }.sortedByDescending { it.time }

                                postList.clear()
                                postList.addAll(posts)
                                postAdapter.notifyDataSetChanged()
                                    if(posts.isNotEmpty()) {
                                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                        layoutManager.smoothScrollToPosition(recyclerView, null, 0);

                                    }
                            }
                            .addOnFailureListener { exception ->
                                Log.d("PostActivity", "Error getting posts: ", exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("PostActivity", "Error getting users: ", exception)
                    }
            }
        }.addOnFailureListener { exception ->
            Log.w("PostActivity", "Error getting friends: ", exception)
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
    private fun navigateToIndividualPost(post: Post) {
        val intent = Intent(this, IndividualPost::class.java)
        intent.putExtra("current", currentUser)
        intent.putExtra("target", post.uid)
        intent.putExtra("avatar", post.profilePic)
        intent.putExtra("username", post.username)
        startActivity(intent)
    }
/*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 || requestCode == 0) {
            postList.clear()
            Log.d("PostActivity", "onActivityResult: $resultCode")
            loadPosts()
        }
    }*/
}