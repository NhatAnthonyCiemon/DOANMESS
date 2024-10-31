package com.example.doanmess

import HandleOnlineActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddFriend :  HandleOnlineActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendAdapter
    lateinit var backBtn: Button
    private val userList = mutableListOf<Friend>()
    private lateinit var searchFilter: EditText
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currentUser = auth.currentUser
    override fun onStart() {
        super.onStart()
        Log.d("Activity", "onStart2")
    }
    override fun onPause() {
        super.onPause()
        Log.d("Activity", "onStop2")
    }
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
        searchFilter = findViewById(R.id.filter_search)
        searchFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filter = s.toString()
                if (filter.isEmpty()) {
                    adapter.changeList(userList)
                } else {
                    val filterLowerCase = filter.toLowerCase()
                    val filteredList = userList.filter { it.name.toLowerCase().contains(filterLowerCase) }
                    adapter.changeList(filteredList.toMutableList())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        fetchUsers()
    }

    private fun fetchUsers() {
        currentUser?.let { user ->
            db.collection("users").get().addOnSuccessListener { result ->
                val friends = mutableListOf<String>()
                val requestSent = mutableListOf<String>()
                val requests = mutableListOf<String>()
                db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                    // Fetch friends and requestSent list of current user
                    if (document.exists()) {
                        friends.addAll(document.get("Friends") as List<String>)
                        requestSent.addAll(document.get("RequestSent") as List<String>)
                        val requestsList = document.get("Requests") as List<Map<String, Any>>
                        requests.addAll(requestsList.map { it["userId"] as String })
                    }
                    // result : all user
                    for (document in result) {
                        val userId = document.id
                        if (userId != user.uid && !friends.contains(userId) && !requests.contains(userId)) {
                            val name = document.getString("Name") ?: ""
                          //  val image = R.drawable.avatar_placeholder_allchat // Placeholder image
                            var imageUrl : String? = document.getString("Avatar")
                           // if(imageUrl=="" || imageUrl==null)
                         //       imageUrl = "https://firebasestorage.googleapis.com/v0/b/doan-cb428.appspot.com/o/avatars%2F3a1a9f11-a045-4072-85da-7202c9bc9989.jpg?alt=media&token=4f3a7b0d-7c87-443f-9e1d-4222f8d22bb9"
                            val reqFriend = requestSent.contains(userId)
                            userList.add(Friend(userId, name, imageUrl!!, reqFriend))
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
        val currentTime = System.currentTimeMillis()
        if (reqFriend) {
            userRef.update("RequestSent", FieldValue.arrayUnion(id))
            targetUserRef.update(mapOf(
                "Requests" to FieldValue.arrayUnion(mapOf(
                    "userId" to currentUserId,
                    "timeSent" to currentTime
                ))
            ))
                .addOnSuccessListener {
                    Toast.makeText(this, "Friend request sent successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send friend request", Toast.LENGTH_SHORT).show()
                }
        } else {
            userRef.update("RequestSent", FieldValue.arrayRemove(id))

            targetUserRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val requests = document.get("Requests") as List<Map<String, Any>>
                    val requestToRemove = requests.find { it["userId"] == currentUserId }
                    if (requestToRemove != null) {
                        targetUserRef.update("Requests", FieldValue.arrayRemove(requestToRemove))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Friend request removed successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to remove friend request", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }
    }
}