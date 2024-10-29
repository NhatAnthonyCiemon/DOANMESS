package com.example.doanmess

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddFriend : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendAdapter
    lateinit var backBtn: Button
    private val userList = mutableListOf<Friend>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        recyclerView = findViewById(R.id.rvAdd)
        adapter = FriendAdapter(userList) { id, reqFriend ->
            updateFriendRequest(id, reqFriend)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        backBtn = findViewById(R.id.btnBack)
        backBtn.setOnClickListener {
            finish()
        }
        fetchUsers()
    }

    private fun fetchUsers() {
        currentUser?.let { user ->
            db.collection("users").get().addOnSuccessListener { result ->
                val friends = mutableListOf<String>()
                val requestSent = mutableListOf<String>()
                db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                    // Fetch friends and requestSent list of current user
                    if (document.exists()) {
                        friends.addAll(document.get("friends") as List<String>)
                        requestSent.addAll(document.get("requestSent") as List<String>)
                    }
                    // result : all user
                    for (document in result) {
                        val userId = document.id
                        if (userId != user.uid && !friends.contains(userId)) {
                            val name = document.getString("name") ?: ""
                            val image = R.drawable.avatar_placeholder_allchat // Placeholder image
                            val reqFriend = requestSent.contains(userId)
                            userList.add(Friend(userId, name, image, reqFriend))
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFriendRequest(id: String, reqFriend: Boolean) {
        val currentUserId = currentUser!!.uid
        val userRef = db.collection("users").document(currentUserId)
        val targetUserRef = db.collection("users").document(id)
        if (reqFriend) {
            userRef.update("requestSent", FieldValue.arrayUnion(id))
            targetUserRef.update("requests", FieldValue.arrayUnion(currentUserId))
                .addOnSuccessListener {
                    Toast.makeText(this, "Friend request sent successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send friend request", Toast.LENGTH_SHORT).show()
                }
        } else {
            userRef.update("requestSent", FieldValue.arrayRemove(id))
            targetUserRef.update("requests", FieldValue.arrayRemove(currentUserId))
                .addOnSuccessListener {
                    Toast.makeText(this, "Friend request removed successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to remove friend request", Toast.LENGTH_SHORT).show()
                }
        }
    }
}