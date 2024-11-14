package com.example.createuiproject

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.example.doanmess.InforChat
import com.example.doanmess.MessageController
import com.example.doanmess.R
import com.example.doanmess.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.util.UUID
import android.Manifest
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

class MainChat : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val REQUEST_CODE_PICK_MEDIA = 100
    private lateinit var valueEventListener: ValueEventListener
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var message_input: EditText
    private lateinit var database: DatabaseReference
    private lateinit var attachButton : ImageView
    private val chatMessages = mutableListOf<ChatMessage>()
    private var lastSenderId: String = ""
    private val messageController = MessageController()
    private var isGroup = false
    private var currentUserUid: String = ""
    private var targetUserUid: String = ""
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private val storage = FirebaseStorage.getInstance()
    private lateinit var locationBtn: ImageButton
    data class ChatMessage(
        val content: String = "",
        val sendId: String = "",
        val recvId: String = "",
        val type: String = "",
     //   val status: Boolean = false,
        val time: Long = 0L,
        var showSenderInfo: Boolean = false
    ) {
        var senderName: String = ""
        var avatarUrl: String = ""
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top , systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        currentUserUid = auth.currentUser!!.uid
        targetUserUid = intent.getStringExtra("uid") ?: return

        isGroup = intent.getBooleanExtra("isGroup", false)
        // Set the user name and avatar
        val name = intent.getStringExtra("name") ?: "User"
        findViewById<android.widget.TextView>(R.id.user_name).text = name
        val avatar = intent.getStringExtra("avatar") ?: ""
        val avatarView = findViewById<ImageView>(R.id.user_avatar)
        attachButton = findViewById(R.id.attach_button)
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



        // Set up the RecyclerView
        val chatAdapter = ChatAdapter(chatMessages, isGroup)
        recyclerViewMessages = findViewById<RecyclerView>(R.id.main_chat_recycler)
        recyclerViewMessages.isVerticalScrollBarEnabled = false;
        recyclerViewMessages.adapter = chatAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)

        if (!isGroup) {
            if (currentUserUid != null && targetUserUid != null) {
                //set status of user in list chat of last item  to true
                Firebase.database.getReference("users").child(currentUserUid!!)
                    .child(targetUserUid).child("Status").setValue(true)
                database = Firebase.database.getReference("users").child(currentUserUid)
                        .child(targetUserUid).child("Messages")
                valueEventListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Firebase.database.getReference("users").child(currentUserUid!!)
                            .child(targetUserUid).child("Status").setValue(true)
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
                            val time =
                                messageSnapshot.child("Time").getValue(Long::class.java) ?: 0L
                            val type =
                                messageSnapshot.child("Type").getValue(String::class.java) ?: "text"
                            val chatMessage = ChatMessage(
                                content = content,
                                sendId = sendId,
                                recvId = recvId,
                                time = time,
                                type = type
                            ).apply {
                                this.senderName = senderName
                                this.avatarUrl = avatarUrl
                            }
                            // Check if the sender is different from the last one
                            chatMessage.showSenderInfo = sendId != lastSenderId
                            lastSenderId = sendId // Update the last sender

                            chatMessages.add(chatMessage)

                            // Refresh your adapter or UI component here
                            chatAdapter.notifyDataSetChanged()

                            recyclerViewMessages.scrollToPosition(chatMessages.size - 1)

                        }
                        recyclerViewMessages.scrollToPosition(chatMessages.size - 1) // Scroll to bottom
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                }
                //sort chatmessages list by time

                database.addValueEventListener(valueEventListener)
            }
        }
        else {
            //set status of user of Status of that Group to true
            Firebase.database.getReference("groups").child(targetUserUid).child("Status").child(currentUserUid!!).setValue(true)
            database = Firebase.database.getReference("groups").child(targetUserUid).child("Messages")
            valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatMessages.clear() // Clear previous data
                 //   Log.d("MainChat", "onDataChange: ${snapshot.key}")
                    Firebase.database.getReference("groups").child(targetUserUid).child("Status").child(currentUserUid!!).setValue(true)

                    lastSenderId = "" // Reset lastSenderId for fresh load

                    val tempMessages = mutableListOf<ChatMessage>()

                    for (messageSnapshot in snapshot.children) {
                        val content = messageSnapshot.child("Content").getValue(String::class.java) ?: ""
                        val sendId = messageSnapshot.child("SendId").getValue(String::class.java) ?: ""
                        val recvId = messageSnapshot.child("RecvId").getValue(String::class.java) ?: ""
                        val time = messageSnapshot.child("Time").getValue(Long::class.java) ?: 0L

                        val chatMessage = ChatMessage(
                            content = content,
                            sendId = sendId,
                            recvId = recvId,
                            time = time
                        )

                        tempMessages.add(chatMessage)
                    }

                    tempMessages.sortBy { it.time }
                    chatMessages.addAll(tempMessages) // Update chatMessages with sorted list
                    chatAdapter.notifyDataSetChanged() // Notify adapter of data change
                    recyclerViewMessages.scrollToPosition(chatMessages.size - 1) // Scroll to bottom

                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle database error if needed
                }
            }
            database.addValueEventListener(valueEventListener)
        }


        checkBlockedStatus()

        // set on click listener for the back button to navigate back to the home activity
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Set on click listener for the info button to navigate to the user info activity
        findViewById<ImageButton>(R.id.info_button).setOnClickListener {
            // Navigate to user info activity
            val intent = Intent(this, InforChat::class.java)
            intent.putExtra("uid", targetUserUid) // Pass the uid to InforChat
            startActivity(intent)
        }

        // Set on click listener for the name_layout to navigate to the user info activity
        findViewById<LinearLayout>(R.id.name_layout).setOnClickListener {
            // Navigate to user info activity
            val intent = Intent(this, InforChat::class.java)
            intent.putExtra("uid", targetUserUid) // Pass the uid to InforChat
            startActivity(intent)
        }

        message_input = findViewById<android.widget.EditText>(R.id.message_input)
        // set on click listener for the send button to send the message
        findViewById<ImageButton>(R.id.send_button).setOnClickListener {
            val message = message_input.text.toString()
            sendMessage(message,"text")
        }
        attachButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            val mimeTypes = arrayOf("image/*", "video/*")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA)
        }
        // set on click listener for the mic button to start voice recording
        val micButton = findViewById<ImageView>(R.id.mic_button)
        findViewById<ImageView>(R.id.mic_button).setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Start voice recording
                    startRecordingCheck()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Stop recording and save to Firebase
                    stopRecordingAndSave()
                    v.performClick() // Call performClick for accessibility
                    true
                }
                else -> false
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationBtn = findViewById(R.id.location_button)
        locationBtn.setOnClickListener {
            //send current location
            sendCurrentLocation()
        }
    }

    private fun sendCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                val locationMessage = "Location: https://maps.google.com/?q=$latitude,$longitude"
                sendMessage(locationMessage, "location")
            } ?: run {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun sendMessage(message: String, type: String) {
        if (!isGroup) {
            if (message.isNotEmpty()) {
                Firebase.database.getReference("users").child(currentUserUid!!)
                    .child(targetUserUid).child("Status").setValue(true)
                Firebase.database.getReference("users").child(targetUserUid)
                    .child(currentUserUid).child("Status").setValue(false)
                /*val chatMessage = ChatMessage(
                    content = message,
                    sendId = currentUserUid ?: "",
                    recvId = targetUserUid,
                    //   status = false,
                    time = System.currentTimeMillis()
                )*/
                //  chatMessages.add(chatMessage)
                //  chatAdapter.notifyDataSetChanged()
                recyclerViewMessages.scrollToPosition(chatMessages.size - 1)
                message_input.text.clear()
                // Save the message to the database
                val newMessage = mapOf(
                    "Content" to message,
                    "SendId" to currentUserUid,
                    "RecvId" to targetUserUid,
                    "Time" to System.currentTimeMillis(),
                    "Type" to type
                )
                Firebase.database.getReference("users").child(currentUserUid!!)
                    .child(targetUserUid).child("Messages").push().setValue(newMessage)
                if(currentUserUid == targetUserUid) {
                    return
                }
                //Save for target user
                Firebase.database.getReference("users").child(targetUserUid!!)
                    .child(currentUserUid).child("Messages").push().setValue(newMessage)
                var noti = message
                if(type == "audio") {
                    noti = "Sent an audio"
                }
                messageController.newMessageFriend(targetUserUid, currentUserUid, noti )
            }
        }
        else {
            if (message.isNotEmpty() || type != "text") {
                Firebase.database.getReference("groups").child(targetUserUid).child("Status").child(currentUserUid!!).setValue(true)
                val groupData = Firebase.firestore.collection("groups").document(targetUserUid)
                if(groupData != null) {
                    groupData.get().addOnSuccessListener { document ->
                        val users = document.get("Participants") as List<String>
                        if (users != null) {
                            for (user in users) {
                                if (user != currentUserUid) {
                                    Firebase.database.getReference("groups").child(targetUserUid).child("Status").child(user).setValue(false)
                                }
                            }
                            // After updating the status, push the new message
           /*                 val chatMessage = ChatMessage(
                                content = message,
                                sendId = currentUserUid ?: "",
                                recvId = targetUserUid,
                                time = System.currentTimeMillis()
                            )*/
                            message_input.text.clear()
                            val newMessage = mapOf(
                                "Content" to message,
                                "SendId" to currentUserUid,
                                "RecvId" to targetUserUid,
                                "Time" to System.currentTimeMillis(),
                                "Type" to type
                            )
                            database.push().setValue(newMessage)
                            messageController.newMessageGroup(targetUserUid, currentUserUid, message)
                        }
                    }
                }
            }
        }
    }
    private fun requestPermissions() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainChat", "Requesting permissions")
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 200)
        }
    }
    private fun startRecordingCheck(){
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else {
            requestPermissions()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_MEDIA && resultCode == RESULT_OK) {
            val selectedMediaUri: Uri? = data?.data
            if (selectedMediaUri != null) {
                uploadMediaToFirebase(selectedMediaUri)
            }
        }
    }
    private fun uploadMediaToFirebase(fileUri: Uri) {
        val storageRef = storage.reference.child("media/${UUID.randomUUID()}")
        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                sendMessage(downloadUrl, "media")
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startRecording() {
        // Release any existing MediaRecorder instance
        mediaRecorder?.release()
        mediaRecorder = null
        // Define the audio file path
        audioFilePath = "${externalCacheDir?.absolutePath}/audiorecord123.wav"
        // Delete the previous file if it exists
        val audioFile = File(audioFilePath)
        if (audioFile.exists()) {
            audioFile.delete()
        }
        // Initialize and start the MediaRecorder
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            try {
                prepare()
                start()
                Toast.makeText(this@MainChat, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this@MainChat, "Recording failed", Toast.LENGTH_SHORT).show()
            } catch (e: IllegalStateException) {
                Toast.makeText(this@MainChat, "Recording failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start recording
             //   startRecording()
            } else {
                // Permission denied
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
            return
        }
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
         //   sendCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private fun stopRecordingAndSave() {

        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: RuntimeException) {
                // Handle the case where stop() is called before start() or if there was an error during recording
                Log.e("MainChat", "stop() failed", e)
            } finally {
                release()
            }
        }
        mediaRecorder = null

        val wavFilePath = audioFilePath


        val mp3FilePath = "${externalCacheDir?.absolutePath}/audiorecord123.mp3"
        val audioFile = File(mp3FilePath)
        if (audioFile.exists()) {
            audioFile.delete()
        }
        // Convert wav to mp3 using FFmpeg
        wavFilePath?.let {
            val command = arrayOf("-i", it, mp3FilePath)
            FFmpeg.executeAsync(command) { _, returnCode ->
                if (returnCode == RETURN_CODE_SUCCESS) {
                    val audioFileUri = Uri.fromFile(File(mp3FilePath))
                    val audioRef = storage.reference.child("audio/${UUID.randomUUID()}.mp3")

                    audioRef.putFile(audioFileUri)
                        .addOnSuccessListener {
                            audioRef.downloadUrl.addOnSuccessListener { uri ->
                                //send link to that audio with type audio
                                sendMessage(uri.toString(), "audio")
                            }
                            Toast.makeText(this@MainChat, "Recording saved", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@MainChat, "Failed to save recording", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@MainChat, "Failed to convert recording", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        checkBlockedStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::valueEventListener.isInitialized) {
            if(intent.getBooleanExtra("isGroup", false)) {
                database= Firebase.database.getReference("groups").child(intent.getStringExtra("uid") ?: "").child("Messages")
            }
            else {
                database =
                    Firebase.database.getReference("users").child(auth.currentUser?.uid ?: "")
                        .child(intent.getStringExtra("uid") ?: "").child("Messages")
            }
            database.removeEventListener(valueEventListener)
        }
    }
    private fun checkBlockedStatus() {
        val inputBar = findViewById<LinearLayout>(R.id.input_bar)
        val blockedMessage = findViewById<TextView>(R.id.blocked_message)
        val userId = auth.currentUser?.uid ?: return
        val targetUserUid = intent.getStringExtra("uid") ?: return

        Firebase.firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val blockedUsers = document["Blocks"] as? List<Map<String, Any>>
                    if (blockedUsers != null) {
                        for (blockedUser in blockedUsers) {
                            val uid = blockedUser["uid"] as? String
                            if (uid == targetUserUid) {
                                // Hide input bar and show blocked message
                                inputBar.visibility = View.GONE
                                blockedMessage.visibility = View.VISIBLE
                                return@addOnSuccessListener
                            }
                        }
                    }
                }
                // If not blocked, show input bar and hide blocked message
                inputBar.visibility = View.VISIBLE
                blockedMessage.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Handle error
                inputBar.visibility = View.VISIBLE
                blockedMessage.visibility = View.GONE
            }
    }
}