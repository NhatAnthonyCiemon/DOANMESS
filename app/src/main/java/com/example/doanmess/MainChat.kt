package com.example.createuiproject

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doanmess.InforChat
import com.example.doanmess.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore

class MainChat : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val chatMessages = mutableListOf<ChatMessage>()
    private var lastSenderId: String = ""

    data class ChatMessage(
        val content: String = "",
        val sendId: String = "",
        val recvId: String = "",
        val status: Boolean = false,
        val time: Long = 0L,
        var showSenderInfo: Boolean = false
    ) {
        var senderName: String = ""
        var avatarUrl: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_chat)

        auth = Firebase.auth
        val currentUserUid = auth.currentUser?.uid
        val targetUserUid = intent.getStringExtra("uid") ?: return


        // Set the user name and avatar
        val name = intent.getStringExtra("name") ?: "User"
        findViewById<android.widget.TextView>(R.id.user_name).text = name
        val avatar = intent.getStringExtra("avatar") ?: ""
        val avatarView = findViewById<ImageView>(R.id.user_avatar)
        if (avatar.isNotEmpty()) {
            Glide.with(this)
                .load(avatar)
                .circleCrop()
                .placeholder(R.drawable.ic_avatar) // Optional placeholder
                .into(avatarView)
        }

//        // Set the user status
//        var isOnline = true
//        database = Firebase.database.getReference("users").child(targetUserUid).child("online")
//        database.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                isOnline = snapshot.getValue(Boolean::class.java) ?: false
//                if (isOnline) {
//                    findViewById<android.widget.TextView>(R.id.user_status).text = "Online"
//                } else {
//                    findViewById<android.widget.TextView>(R.id.user_status).text = "Offline"
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//            }
//        })


        val isGroup = intent.getBooleanExtra("isGroup", false)
        // Set up the RecyclerView
        val chatAdapter = ChatAdapter(chatMessages, isGroup)
        val recyclerViewMessages = findViewById<RecyclerView>(R.id.main_chat_recycler)
        recyclerViewMessages.adapter = chatAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)

        if (!isGroup) {
            if (currentUserUid != null && targetUserUid != null) {
                database =
                    Firebase.database.getReference("users").child(currentUserUid)
                        .child(targetUserUid)

                database.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val senderName = name
                        val avatarUrl = avatar
                        chatMessages.clear() // Clear previous data
                        lastSenderId = "" // Reset lastSenderId for fresh load

                        for (messageSnapshot in snapshot.children) {
                            // Manually extract each field and handle null cases
                            val content =
                                messageSnapshot.child("Content").getValue(String::class.java) ?: ""
                            val sendId =
                                messageSnapshot.child("SendId").getValue(String::class.java) ?: ""
                            val recvId =
                                messageSnapshot.child("RecvId").getValue(String::class.java) ?: ""
                            val status =
                                messageSnapshot.child("Status").getValue(Boolean::class.java)
                                    ?: false
                            val time =
                                messageSnapshot.child("Time").getValue(Long::class.java) ?: 0L

                            val chatMessage = ChatMessage(
                                content = content,
                                sendId = sendId,
                                recvId = recvId,
                                status = status,
                                time = time
                            ).apply {
                                this.senderName = senderName
                                this.avatarUrl = avatarUrl
                            }
                            // Check if the sender is different from the last one
                            chatMessage.showSenderInfo = sendId != lastSenderId
                            lastSenderId = sendId // Update the last sender

                            chatMessages.add(chatMessage)

                            // Refresh your adapter or UI component here
                        }
                        chatAdapter.notifyDataSetChanged()

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }
        else {
//            database = Firebase.database.getReference("groups").child(targetUserUid)
//            database.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
////                    val senderName = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
////                    val avatarUrl = snapshot.child("avatar").getValue(String::class.java) ?: ""
//
//                    chatMessages.clear() // Clear previous data
//                    lastSenderId = "" // Reset lastSenderId for fresh load
//
//                    for (messageSnapshot in snapshot.children) {
//                        // Manually extract each field and handle null cases
//                        val content =
//                            messageSnapshot.child("Content").getValue(String::class.java) ?: ""
//                        val sendId =
//                            messageSnapshot.child("SendId").getValue(String::class.java) ?: ""
//                        val recvId =
//                            messageSnapshot.child("RecvId").getValue(String::class.java) ?: ""
//                        val status =
//                            messageSnapshot.child("Status").getValue(Boolean::class.java)
//                                ?: false
//                        val time =
//                            messageSnapshot.child("Time").getValue(Long::class.java) ?: 0L
//
//                        val database2 = Firebase.firestore.collection("users").document(sendId)
//                        val senderName = database2.get().result?.getString("Name") ?: "Unknown"
//                        val avatarUrl = database2.get().result?.getString("Avatar") ?: ""
//
//                        val chatMessage = ChatMessage(
//                            content = content,
//                            sendId = sendId,
//                            recvId = recvId,
//                            status = status,
//                            time = time
//                        ).apply {
//                            this.senderName = senderName
//                            this.avatarUrl = avatarUrl
//                        }
//                        // Check if the sender is different from the last one
//                        chatMessage.showSenderInfo = sendId != lastSenderId
//                        lastSenderId = sendId // Update the last sender
//                        chatMessages.add(chatMessage)
//                        // Refresh your adapter or UI component here
//                    }
//                    chatAdapter.notifyDataSetChanged()
//                }
//                override fun onCancelled(error: DatabaseError) {
//                }
//            })
//        }
            database = Firebase.database.getReference("groups").child(targetUserUid)
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatMessages.clear() // Clear previous data
                    lastSenderId = "" // Reset lastSenderId for fresh load

                    // Use a counter to keep track of how many Firestore queries have completed
//                    var completedQueries = 0
                    val totalMessages = snapshot.childrenCount

                    for (messageSnapshot in snapshot.children) {
                        // Manually extract each field and handle null cases
                        val content = messageSnapshot.child("Content").getValue(String::class.java) ?: ""
                        val sendId = messageSnapshot.child("SendId").getValue(String::class.java) ?: ""
                        val recvId = messageSnapshot.child("RecvId").getValue(String::class.java) ?: ""
                        val status = messageSnapshot.child("Status").getValue(Boolean::class.java) ?: false
                        val time = messageSnapshot.child("Time").getValue(Long::class.java) ?: 0L

                       // Fetch sender info from Firestore
                        val database2 = Firebase.firestore.collection("users").document(sendId)
                        database2.get().addOnSuccessListener { document ->
                            val senderName = document.getString("Name") ?: "Unknown"
                            val avatarUrl = document.getString("Avatar") ?: ""

                            // Create and populate the chat message
                            val chatMessage = ChatMessage(
                                content = content,
                                sendId = sendId,
                                recvId = recvId,
                                status = status,
                                time = time
                            ).apply {
                                this.senderName = senderName
                                this.avatarUrl = avatarUrl
                                // Check if the sender is different from the last one
                                this.showSenderInfo = sendId != lastSenderId
                            }

                            // Update lastSenderId
                            lastSenderId = sendId // Update the last sender
                            chatMessages.add(chatMessage)

                            chatAdapter.notifyDataSetChanged() // Refresh your adapter or UI component here


                            // Notify the adapter only when all Firestore queries are completed
//                            completedQueries++
//                            if (completedQueries == totalMessages.toInt()) {
//                                chatAdapter.notifyDataSetChanged() // Refresh your adapter or UI component here
//                            }
                        }.addOnFailureListener {
                            // Handle Firestore retrieval failure if needed
                            val chatMessage = ChatMessage(
                                content = content,
                                sendId = sendId,
                                recvId = recvId,
                                status = status,
                                time = time
                            ).apply {
                                this.senderName = "Unknown"
                                this.avatarUrl = ""
                                this.showSenderInfo = sendId != lastSenderId
                            }

                            lastSenderId = sendId // Update the last sender
                            chatMessages.add(chatMessage)
                            chatAdapter.notifyDataSetChanged() // Refresh your adapter or UI component here

                            // Update completedQueries
//                            completedQueries++
//                            if (completedQueries == totalMessages.toInt()) {
//                                chatAdapter.notifyDataSetChanged() // Refresh your adapter or UI component here
//                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error if needed
                }
            })
        }


            // Get the display metrics
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        // Set RecyclerView height to a percentage of the screen height (e.g., 70%)
        val layoutParams = recyclerViewMessages.layoutParams
        layoutParams.height = (screenHeight * 0.75).toInt() // Change the percentage as needed
        recyclerViewMessages.layoutParams = layoutParams

        //set the input bar width to 70%
        val inputBar = findViewById<LinearLayout>(R.id.input_bar)
        val inputBarLayoutParams = inputBar.layoutParams
        inputBarLayoutParams.width = (screenHeight * 0.70).toInt()
        inputBar.layoutParams = inputBarLayoutParams

        // set on click listener for the back button to navigate back to the home activity
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        //set on click listener for the info button to navigate to the user info activity
        findViewById<ImageButton>(R.id.info_button).setOnClickListener {
            // Navigate to user info activity
            val intent = Intent(this, InforChat::class.java)
            startActivity(intent)
        }

        //set on click listener for the name_layout to navigate to the user infor activity
        findViewById<LinearLayout>(R.id.name_layout).setOnClickListener {
            // Navigate to user info activity
            val intent = Intent(this, InforChat::class.java)
            startActivity(intent)
        }

        // set on click listener for the mic button to start voice recording
        findViewById<ImageView>(R.id.mic_button).setOnClickListener {
            // Start voice recording
        }

        val message_input = findViewById<android.widget.EditText>(R.id.message_input)
        // set on click listener for the send button to send the message
        findViewById<FrameLayout>(R.id.send_button).setOnClickListener {
            val message = message_input.text.toString()
            if (!isGroup) {
                if (message.isNotEmpty()) {
                    val chatMessage = ChatMessage(
                        content = message,
                        sendId = currentUserUid ?: "",
                        recvId = targetUserUid,
                        status = false,
                        time = System.currentTimeMillis()
                    )
                    chatMessages.add(chatMessage)
                    chatAdapter.notifyDataSetChanged()
                    message_input.text.clear()
                    // Save the message to the database
                    database = Firebase.database.getReference("users").child(currentUserUid!!)
                        .child(targetUserUid)
                    val newMessage = database.push()
                    newMessage.child("Content").setValue(chatMessage.content)
                    newMessage.child("SendId").setValue(chatMessage.sendId)
                    newMessage.child("RecvId").setValue(chatMessage.recvId)
                    newMessage.child("Status").setValue(chatMessage.status)
                    newMessage.child("Time").setValue(chatMessage.time)
                }
            }
            else {
                if (message.isNotEmpty()) {
                    val chatMessage = ChatMessage(
                        content = message,
                        sendId = currentUserUid ?: "",
                        recvId = targetUserUid,
                        status = false,
                        time = System.currentTimeMillis()
                    )
                    chatMessages.add(chatMessage)
                    chatAdapter.notifyDataSetChanged()
                    message_input.text.clear()
                    // Save the message to the database
                    database = Firebase.database.getReference("groups").child(targetUserUid)
                    val newMessage = database.push()
                    newMessage.child("Content").setValue(chatMessage.content)
                    newMessage.child("SendId").setValue(chatMessage.sendId)
                    newMessage.child("RecvId").setValue(chatMessage.recvId)
                    newMessage.child("Status").setValue(chatMessage.status)
                    newMessage.child("Time").setValue(chatMessage.time)
                }
            }
        }
    }
}