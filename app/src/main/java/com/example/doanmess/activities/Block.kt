package com.example.doanmess.activities


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doanmess.adapters.BlockAdapter
import com.example.doanmess.models.BlockModel
import com.example.doanmess.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Block : HandleOnlineActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlockAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val blockLists = mutableListOf<BlockModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_block)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BlockAdapter(blockLists, supportFragmentManager)
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
                blockLists.clear()
                if (document.exists()) {
                    val blockedUsers = document["Blocks"] as? List<String>
                    if (blockedUsers.isNullOrEmpty()) {
                        Log.d("Block", "No blocked users found")
                    } else {
                        for (blockedUser in blockedUsers) {
                            val uid = blockedUser as? String ?: continue
                            fetchUserDetails(uid)
                        }
                    }
                } else {
                    Log.d("Block", "User document does not exist")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching blocked users", Toast.LENGTH_SHORT).show()
                Log.e("Block", "Error fetching blocked users", e)
            }
    }

    private fun fetchUserDetails(blockedUserId: String) {
        firestore.collection("users").document(blockedUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("Name") ?: ""
                    val avatar = document.getString("Avatar") ?: ""
                    Log.d("Block", "Fetched User Details - Name: $name, Avatar: $avatar")
                    val block = BlockModel(uid = blockedUserId, name = name, avatar = avatar)
                    blockLists.add(block)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.d("Block", "No user details found for ID: $blockedUserId")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user details", Toast.LENGTH_SHORT).show()
                Log.e("Block", "Error fetching user details", e)
            }
    }
}