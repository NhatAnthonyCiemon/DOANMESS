package com.example.doanmess.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doanmess.adapters.FriendListAdapter
import com.example.doanmess.models.FriendModel
import com.example.doanmess.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class friendList : HandleOnlineActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendListAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val friendLists = mutableListOf<FriendModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_list)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FriendListAdapter(friendLists, supportFragmentManager)
        recyclerView.adapter = adapter

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        fetchBlockedUsers()
    }

    private fun fetchBlockedUsers() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                friendLists.clear()
                if (document.exists()) {
                    val blockedUsers = document["Friends"] as? List<String>
                    if (blockedUsers.isNullOrEmpty()) {
                        Log.d("Friend", "No friends found")
                    } else {
                        for (blockedUser in blockedUsers) {
                            val uid = blockedUser as? String ?: continue
                            fetchUserDetails(uid)
                        }
                    }
                } else {
                    Log.d("Friend", "User document does not exist")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching friends", Toast.LENGTH_SHORT).show()
                Log.e("Friend", "Error fetching friends", e)
            }
    }

    private fun fetchUserDetails(UserId: String) {
        firestore.collection("users").document(UserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("Name") ?: ""
                    val avatar = document.getString("Avatar") ?: ""
                    Log.d("Friend", "Fetched User Details - Name: $name, Avatar: $avatar")
                    val block = FriendModel(uid = UserId, name = name, avatar = avatar)
                    friendLists.add(block)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.d("Block", "No user details found for ID: $UserId")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user details", Toast.LENGTH_SHORT).show()
                Log.e("Block", "Error fetching user details", e)
            }
    }
}