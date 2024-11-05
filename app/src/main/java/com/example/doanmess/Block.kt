package com.example.doanmess

import HandleOnlineActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

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
        adapter = BlockAdapter(blockLists)
        recyclerView.adapter = adapter

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        fetchBlockedUsers()
    }


//    working on
private fun fetchBlockedUsers() {
    val userId = auth.currentUser?.uid ?: return
    if (firestore == null) {
        Toast.makeText(this, "Firestore is not initialized", Toast.LENGTH_SHORT).show()
        return
    }
    firestore.collection("users").document(userId)
        .get()
        .addOnSuccessListener { document ->
            blockLists.clear()
            if (document.exists()) {
                val blockedUserIds = document["Blocks"] as? List<String>
                if (blockedUserIds.isNullOrEmpty()) {
                    Log.d("Block", "No blocked users found")
                } else {
                    for (blockedUserId in blockedUserIds) {
                        fetchUserDetails(blockedUserId)
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
                    val block = BlockModel(uid = blockedUserId, name = name, avatar = avatar, timestamp = "")
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

    private fun blockUser(blockedUserId: String, blockedUserName: String, blockedUserAvatar: String) {
        val userId = auth.currentUser?.uid ?: return
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val block = BlockModel(name = blockedUserName, avatar = blockedUserAvatar, timestamp = timestamp)
        firestore.collection("users").document(userId).collection("Blocks").document(blockedUserId)
            .set(block)
            .addOnSuccessListener {
                Toast.makeText(this, "User blocked successfully", Toast.LENGTH_SHORT).show()
                fetchBlockedUsers()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error blocking user", Toast.LENGTH_SHORT).show()
            }
    }
}