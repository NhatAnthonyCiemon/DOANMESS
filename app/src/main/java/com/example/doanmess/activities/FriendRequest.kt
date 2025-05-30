package com.example.doanmess.activities

import android.os.Bundle
import android.text.format.DateUtils
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doanmess.adapters.FriendRequestAdapter
import com.example.doanmess.models.FriendRequestModel
import com.example.doanmess.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendRequest  : HandleOnlineActivity() {

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
                            var profileImg = userDoc.getString("Avatar") ?: "https://firebasestorage.googleapis.com/v0/b/doan-cb428.appspot.com/o/avatars%2F3a1a9f11-a045-4072-85da-7202c9bc9989.jpg?alt=media&token=4f3a7b0d-7c87-443f-9e1d-4222f8d22bb9"
                            if (profileImg==null || profileImg == "") {
                                profileImg = "https://firebasestorage.googleapis.com/v0/b/doan-cb428.appspot.com/o/avatars%2F3a1a9f11-a045-4072-85da-7202c9bc9989.jpg?alt=media&token=4f3a7b0d-7c87-443f-9e1d-4222f8d22bb9"
                            }
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