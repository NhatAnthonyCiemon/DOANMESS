package com.example.doanmess

import android.os.Bundle
import android.text.format.DateUtils
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class FriendRequest : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendRequestAdapter

    // Dữ liệu giả lập
/*    private val friendRequests = mutableListOf(
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
    )*/
    private val friendRequests : MutableList<FriendRequestModel> = mutableListOf(
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
        fetchFriendRequests()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FriendRequestAdapter(friendRequests)
        recyclerView.adapter = adapter
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }
    private fun fetchFriendRequests() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        currentUser?.let { user ->
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val requests = document.get("Requests") as List<Map<String, Any>>
                    for (request in requests) {
                        val requestId = request["userId"] as String
                        val timeSent = request["timeSent"] as Long
                        db.collection("users").document(requestId).get().addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("Name") ?: ""
                            val profileImg = userDoc.getString("Avatar") ?: ""
                            val time = DateUtils.getRelativeTimeSpanString(timeSent, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
                            friendRequests.add(FriendRequestModel(requestId,profileImg!!, name, time))
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }
}