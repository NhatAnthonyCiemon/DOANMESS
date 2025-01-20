package com.example.doanmess.activities

import com.github.dhaval2404.imagepicker.ImagePicker
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doanmess.adapters.ChatAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID
import com.example.doanmess.helper.MessageController
import com.example.doanmess.helper.OnMessageLongClickListener
import com.example.doanmess.R
import com.example.doanmess.adapters.SeenAdapter
import com.example.doanmess.helper.AESUtils
import com.example.doanmess.models.ImageSeen

class MainChat  : HandleOnlineActivity(), OnMessageLongClickListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var limitMessage = 20
    private val REQUEST_CODE_PICK_MEDIA = 100
    private lateinit var valueEventListener: ValueEventListener
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var message_input: EditText
    private lateinit var database: Query
    private lateinit var recyclerSeen: RecyclerView
    private lateinit var seenAdapter: SeenAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private var lastSenderId: String = ""
    private val messageController = MessageController()
    private var isGroup = false
    private var currentUserUid: String = ""
    private var targetUserUid: String = ""
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private val storage = FirebaseStorage.getInstance()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var videoCallBtn: ImageButton
    private lateinit var callVoiceBtn: ImageButton
    private val imageSeenList = mutableListOf<ImageSeen>()
    private var userStatusListener: ValueEventListener? = null
    private var groupStatusListener: ValueEventListener? = null
    private var avatarUrlMapping: MutableMap<String, String> = mutableMapOf()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var isBlocked = false
    private lateinit var blockListener: ListenerRegistration
    data class ChatMessage(
        val chatId : String = "",
        val content: String = "",
        val sendId: String = "",
        val recvId: String = "",
        val type: String = "",
        var fileName: String = "",
     //   val status: Boolean = false,
        val time: Long = 0L,
        var showSenderInfo: Boolean = false,
        var isSent : Boolean = false,
        val pinned : Boolean = false
    ) {
        var senderName: String = ""
        var avatarUrl: String = ""
    }
//

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
        supportActionBar?.hide()
        auth = Firebase.auth
        currentUserUid = auth.currentUser!!.uid
        targetUserUid = intent.getStringExtra("uid") ?: return
        isGroup = intent.getBooleanExtra("isGroup", false)
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



        videoCallBtn = findViewById(R.id.videoCallBtn)
        callVoiceBtn = findViewById(R.id.callVoiceBtn)
        // Set up the RecyclerView
        chatAdapter = ChatAdapter(chatMessages, isGroup, listener = this, avatarUrlMapping)
        fetchAvatarUrls(targetUserUid, currentUserUid) {
            // Dữ liệu avatarUrlMapping đã sẵn sàng
            chatAdapter.notifyDataSetChanged() // Làm mới RecyclerView để hiển thị avatar
        }

        recyclerViewMessages = findViewById<RecyclerView>(R.id.main_chat_recycler)
        recyclerViewMessages.isVerticalScrollBarEnabled = false;
        recyclerViewMessages.adapter = chatAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        recyclerSeen = findViewById(R.id.recyclerSeen)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        recyclerSeen.layoutManager = layoutManager
        seenAdapter = SeenAdapter(imageSeenList)
        recyclerSeen.adapter = seenAdapter

        recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    loadOlderMessages()
                }
            }
        })

        chatAdapter.setOnItemClickListener(object : ChatAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val message = chatMessages[position]
                if (message.type == "location") {
                    val url = message.content.split(" ").getOrNull(1) // Assuming the URL is the second part of the content
                    if (url != null) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainChat, "Invalid URL", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        if (!isGroup) {
            if (currentUserUid != null && targetUserUid != null) {
                // Set the user status
                var isOnline = true
                database = Firebase.database.getReference("users").child(targetUserUid).child("online")
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        isOnline = snapshot.getValue(Boolean::class.java) ?: false
                        if (isOnline) {
                            findViewById<android.widget.TextView>(R.id.user_status).text = "Online"
                        } else {
                            findViewById<android.widget.TextView>(R.id.user_status).text = "Offline"
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
                //set status of user in list chat of last item  to true
                Firebase.database.getReference("users").child(currentUserUid!!)
                    .child(targetUserUid).child("Status").setValue(true)

                database = Firebase.database.getReference("users").child(currentUserUid)
                        .child(targetUserUid).child("Messages").orderByKey().limitToLast(limitMessage)

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
                            var content =
                                messageSnapshot.child("Content").getValue(String::class.java) ?: ""
                            val sendId =
                                messageSnapshot.child("SendId").getValue(String::class.java) ?: ""
                            val recvId =
                                messageSnapshot.child("RecvId").getValue(String::class.java) ?: ""
                            val time =
                                messageSnapshot.child("Time").getValue(Long::class.java) ?: 0L
                            val type =
                                messageSnapshot.child("Type").getValue(String::class.java) ?: "text"
                            val pinned =
                                messageSnapshot.child("Pinned").getValue(Boolean::class.java) ?: false
                            val fileName =
                                messageSnapshot.child("FileName").getValue(String::class.java) ?: ""

                            if (type == "text") {
                                val base64Key = "q+xZ9yXk5F8WlKsbJb4sHg=="

                                // Tạo đối tượng AESUtils
                                val aesUtils = AESUtils()

                                // Giải mã khóa từ chuỗi Base64 thành SecretKey
                                val secretKey = aesUtils.decodeBase64ToSecretKey(base64Key)

                                // Kiểm tra nếu `content` là null hoặc không hợp lệ
                                val contentStr = content?.toString() ?: "Invalid content"

                                // Giải mã nội dung với try-catch
                                val txt = try {
                                    aesUtils.decryptMessage(contentStr, secretKey) // Gọi hàm giải mã từ lớp AESUtils
                                } catch (e: Exception) {
                                    e.printStackTrace() // Ghi log lỗi nếu có
                                    null // Trả về null nếu xảy ra lỗi
                                }

                                // Kiểm tra kết quả giải mã
                                if (txt != null) {
                                    // Gán nội dung giải mã thành công cho `content`
                                    content = txt
                                } else {
                                    // Thực hiện hành động khi giải mã thất bại
                                    println("Decryption failed. Content remains encrypted or invalid.")
                                }
                            }


                            val chatMessage = ChatMessage(
                                chatId = messageSnapshot.key ?: "",
                                content = content,
                                sendId = sendId,
                                recvId = recvId,
                                time = time,
                                type = type,
                                isSent =true,
                                pinned = pinned,
                                fileName = fileName
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
            // Set the user status
            var isOnline = true
            database = Firebase.database.getReference("groups").child(targetUserUid).child("online")
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isOnline = snapshot.getValue(Boolean::class.java) ?: false
                    if (isOnline) {
                        findViewById<android.widget.TextView>(R.id.user_status).text = "Online"
                    } else {
                        findViewById<android.widget.TextView>(R.id.user_status).text = "Offline"
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
            //set status of user of Status of that Group to true
            Firebase.database.getReference("groups").child(targetUserUid).child("Status").child(currentUserUid!!).setValue(true)
            database = Firebase.database.getReference("groups").child(targetUserUid).child("Messages").orderByKey().limitToLast(limitMessage)
            valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatMessages.clear() // Clear previous data
                 //   Log.d("MainChat", "onDataChange: ${snapshot.key}")
                    Firebase.database.getReference("groups").child(targetUserUid).child("Status").child(currentUserUid!!).setValue(true)

                    lastSenderId = "" // Reset lastSenderId for fresh load

                    val tempMessages = mutableListOf<ChatMessage>()

                    for (messageSnapshot in snapshot.children) {
                        var content = messageSnapshot.child("Content").getValue(String::class.java) ?: ""
                        val sendId = messageSnapshot.child("SendId").getValue(String::class.java) ?: ""
                        val recvId = messageSnapshot.child("RecvId").getValue(String::class.java) ?: ""
                        val time = messageSnapshot.child("Time").getValue(Long::class.java) ?: 0L
                        val pinned = messageSnapshot.child("Pinned").getValue(Boolean::class.java) ?: false
                        val type = messageSnapshot.child("Type").getValue(String::class.java) ?: "text"
                        val fileName = messageSnapshot.child("FileName").getValue(String::class.java) ?: ""

                        if (type == "text") {
                            val base64Key = "q+xZ9yXk5F8WlKsbJb4sHg=="

                            // Tạo đối tượng AESUtils
                            val aesUtils = AESUtils()

                            // Giải mã khóa từ chuỗi Base64 thành SecretKey
                            val secretKey = try {
                                aesUtils.decodeBase64ToSecretKey(base64Key)
                            } catch (e: Exception) {
                                e.printStackTrace() // Ghi log nếu xảy ra lỗi khi tạo khóa
                                null // Trả về null nếu có lỗi
                            }

                            if (secretKey != null) {
                                // Kiểm tra nếu `content` là null hoặc không hợp lệ
                                val contentStr = content?.toString() ?: "Invalid content"

                                // Giải mã nội dung với try-catch
                                val txt = try {
                                    aesUtils.decryptMessage(contentStr, secretKey) // Gọi hàm giải mã từ lớp AESUtils
                                } catch (e: Exception) {
                                    e.printStackTrace() // Ghi log lỗi nếu có
                                    null // Trả về null nếu xảy ra lỗi
                                }

                                // Kiểm tra kết quả giải mã
                                if (txt != null) {
                                    // Gán nội dung giải mã thành công cho `content`
                                    content = txt
                                } else {
                                    // Hiển thị lỗi nếu giải mã thất bại
                                    println("Decryption failed. Content remains encrypted or invalid.")
                                }
                            } else {
                                println("Failed to decode Base64 key. Unable to proceed with decryption.")
                            }
                        }


                        val chatMessage = ChatMessage(
                            chatId = messageSnapshot.key ?: "",
                            content = content,
                            sendId = sendId,
                            recvId = recvId,
                            time = time,
                            type = type,
                            isSent = true,
                            pinned = pinned,
                            fileName = fileName
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
        addBlockListener()

        // set on click listener for the back button to navigate back to the home activity
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            //pause the media player if it is playing
            if (::chatAdapter.isInitialized) {
                chatAdapter.releaseResources()
            }
            finish()
        }

        // Set on click listener for the info button to navigate to the user info activity
        findViewById<ImageButton>(R.id.info_button).setOnClickListener {
            // Navigate to user info activity
            val intent = Intent(this, InforChat::class.java)
            intent.putExtra("uid", targetUserUid) // Pass the uid to InforChat
            intent.putExtra("isGroup", isGroup)
            intent.putExtra("isBlocked", isBlocked)
            startActivity(intent)
        }

        // Set on click listener for the name_layout to navigate to the user info activity
        findViewById<LinearLayout>(R.id.name_layout).setOnClickListener {
            // Navigate to user info activity
            val intent = Intent(this, InforChat::class.java)
            intent.putExtra("uid", targetUserUid) // Pass the uid to InforChat
            intent.putExtra("isGroup", isGroup)
            startActivity(intent)
        }
        recyclerViewMessages.addOnLayoutChangeListener(View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                recyclerViewMessages.postDelayed({
                    recyclerViewMessages.smoothScrollToPosition(
                        chatAdapter.itemCount
                    )
                }, 50)
            }
        })
        message_input = findViewById<android.widget.EditText>(R.id.message_input)
        // set on click listener for the send button to send the message
        findViewById<ImageButton>(R.id.send_button).setOnClickListener {
            val aesUtils = AESUtils() // Tạo một instance của class AESUtils

            var message = message_input.text.toString()
            val base64Key = "q+xZ9yXk5F8WlKsbJb4sHg=="

            try {
                val secretKey = aesUtils.decodeBase64ToSecretKey(base64Key) // Gọi hàm từ AESUtils
                val non_encrypted_message = message
                message = aesUtils.encryptMessage(message, secretKey)       // Gọi hàm từ AESUtils
                sendMessage(message, "text","",non_encrypted_message)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error encrypting the message.", Toast.LENGTH_SHORT).show()
            }

        }
        message_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                recyclerViewMessages.scrollToPosition(chatMessages.size - 1)
            }
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


        videoCallBtn.setOnClickListener {
            var intent = Intent(this, Call::class.java)
            if(isGroup){
                intent = Intent(this, CallGroup::class.java)
                intent.putExtra("groupId", targetUserUid)
            }
            else{
                intent.putExtra("friendId", targetUserUid)
            }
            intent.putExtra("call", true)
            intent.putExtra("isVideoCall", true)
            startActivity(intent)
            if(isGroup){
                MessageController().callVideoGroup(targetUserUid, currentUserUid)
            }
            else {
                MessageController().callVideoFriend(targetUserUid, currentUserUid)
            }
        }
        callVoiceBtn.setOnClickListener {
            var intent = Intent(this, Call::class.java)
            if(isGroup){
                intent = Intent(this, CallGroup::class.java)
                intent.putExtra("groupId", targetUserUid)
            }
            else{
                intent.putExtra("friendId", targetUserUid)
            }
            intent.putExtra("call", true)
            intent.putExtra("isVideoCall", false)
            startActivity(intent)
            if(isGroup){
                MessageController().callVoiceGroup(targetUserUid, currentUserUid)
            }
            else {
                MessageController().callVoiceFriend(targetUserUid, currentUserUid)
            }
        }

        val optionsButton: ImageButton = findViewById(R.id.options_button)
        optionsButton.setOnClickListener {
            showOptionsMenu(it)
        }
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                val uri: Uri? = data?.data
                uri?.let {  uploadMediaToFirebase(it,true) }

            }
        }
    }


    private fun loadOlderMessages() {
        val loadingIndicator = findViewById<ProgressBar>(R.id.loading_indicator)
        loadingIndicator.visibility = View.VISIBLE // Show the loading indicator

        val firstMessage = chatMessages.firstOrNull() ?: return
        val firstMessageKey = firstMessage.chatId

        val olderMessagesQuery = if (isGroup) {
            Firebase.database.getReference("groups").child(targetUserUid).child("Messages")
                .orderByKey()
                .endBefore(firstMessageKey)
                .limitToLast(limitMessage)
        } else {
            Firebase.database.getReference("users").child(currentUserUid)
                .child(targetUserUid).child("Messages")
                .orderByKey()
                .endBefore(firstMessageKey)
                .limitToLast(limitMessage)
        }

        olderMessagesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val olderMessages = mutableListOf<ChatMessage>()
                for (messageSnapshot in snapshot.children) {
                    var content = messageSnapshot.child("Content").getValue(String::class.java) ?: ""
                    val sendId = messageSnapshot.child("SendId").getValue(String::class.java) ?: ""
                    val recvId = messageSnapshot.child("RecvId").getValue(String::class.java) ?: ""
                    val time = messageSnapshot.child("Time").getValue(Long::class.java) ?: 0L
                    val type = messageSnapshot.child("Type").getValue(String::class.java) ?: "text"
                    val pinned = messageSnapshot.child("Pinned").getValue(Boolean::class.java) ?: false


                    if (type == "text") {
                        val base64Key = "q+xZ9yXk5F8WlKsbJb4sHg=="
                        val aesUtils = AESUtils()  // Tạo một instance của class AESUtils
                        val secretKey = aesUtils.decodeBase64ToSecretKey(base64Key)  // Gọi hàm decodeBase64ToSecretKey từ AESUtils

                        // Kiểm tra nếu `content` là null hoặc không hợp lệ
                        val contentStr = content?.toString() ?: "Invalid content"

                        // Giải mã nội dung với try-catch
                        val txt = try {
                            aesUtils.decryptMessage(contentStr, secretKey)  // Gọi hàm decryptMessage từ AESUtils
                        } catch (e: Exception) {
                            e.printStackTrace()  // Log lỗi nếu có
                            null  // Trả về null nếu xảy ra lỗi
                        }

                        // Kiểm tra kết quả giải mã
                        if (txt != null) {
                            // Hiển thị nội dung nếu thành công
                            content = txt
                        } else {
                            // Hiển thị lỗi nếu giải mã thất bại
                        }
                    }


                    val chatMessage = ChatMessage(
                        chatId = messageSnapshot.key ?: "",
                        content = content,
                        sendId = sendId,
                        recvId = recvId,
                        time = time,
                        type = type,
                        isSent = true,
                        pinned = pinned
                    )
                    if (chatMessages.none { it.chatId == chatMessage.chatId }) {
                        olderMessages.add(chatMessage)
                    }
                }
                olderMessages.sortBy { it.time }
                chatMessages.addAll(0, olderMessages) // Add older messages to the beginning of the list
                chatAdapter.notifyItemRangeInserted(0, olderMessages.size)
                loadingIndicator.visibility = View.GONE // Hide the loading indicator
            }

            override fun onCancelled(error: DatabaseError) {
                loadingIndicator.visibility = View.GONE // Hide the loading indicator
                // Handle database error if needed
            }
        })
    }
    private fun showOptionsMenu(view: View) {
        val popupMenu = PopupMenu(ContextThemeWrapper(this, R.style.PopupMenuBg), view)
        popupMenu.menuInflater.inflate(R.menu.options_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_location -> {
                    // Handle location button click
                    sendCurrentLocation()
                    true
                }
                R.id.action_attach -> {
                    // Handle attach button click
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    //    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "*/*"
                    val mimeTypes = arrayOf("image/*", "video/*")
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                    startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA)
                    true
                }
                R.id.action_take_picture -> {
                    // Handle take picture button click
                    ImagePicker.with(this)
                        .crop()
                        .cameraOnly()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent { intent -> imagePickerLauncher.launch(intent) }
                    true
                }
                R.id.action_video_recording -> {
                    // Handle video recording button click
                    val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA)
                    }
                    true
                }
                R.id.action_send_file -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "*/*"
                    val mimeTypes = arrayOf(
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "text/plain",
                        "application/zip",
                        "application/x-rar-compressed"
                    )
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                    startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA)
                    true
                }
                else -> false
            }
        }
        val popup = PopupMenu::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val menu = popup.get(popupMenu)
        menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(menu, true)
        popupMenu.show()
    }

    fun fetchAvatarUrls(targetUserUid: String, currentUserUid: String, onComplete: () -> Unit) {
        val avatarUrlMapping = mutableMapOf<String, String>()
        if (isGroup) {
            val groupData = Firebase.firestore.collection("groups").document(targetUserUid)
            groupData.get().addOnSuccessListener { document ->
                val participants = document["Participants"] as List<String>
                var pendingTasks = participants.size

                for (participant in participants) {
                    if (participant != currentUserUid) {
                        Firebase.firestore.collection("users").document(participant).get()
                            .addOnSuccessListener { userDocument ->
                                val userAvatar = userDocument["Avatar"] as? String ?: ""
                                avatarUrlMapping[participant] = userAvatar
                            }
                            .addOnCompleteListener {
                                pendingTasks--
                                if (pendingTasks == 0) {
                                    // All tasks completed
                                    this.avatarUrlMapping.clear()
                                    this.avatarUrlMapping.putAll(avatarUrlMapping)
                                    onComplete() // Notify that data fetching is done
                                }
                            }
                    } else {
                        pendingTasks--
                    }
                }
            }
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

    private fun sendMessage(message: String, type: String, fileName : String = "", non_encrypted_message : String = "") {
        if (!isGroup) {
            if (message.isNotEmpty()) {
                Firebase.database.getReference("users").child(currentUserUid!!)
                    .child(targetUserUid).child("Status").setValue(true)
                Firebase.database.getReference("users").child(targetUserUid)
                    .child(currentUserUid).child("Status").setValue(false)
                val chatMessage = ChatMessage(
                    content = message,
                    sendId = currentUserUid ?: "",
                    recvId = targetUserUid,
                    //   status = false,
                    time = System.currentTimeMillis(),
                    isSent = false,
                    type = type,
                )
              //  chatMessages.add(chatMessage)
            //   chatAdapter.notifyItemInserted(chatMessages.size - 1)
                recyclerViewMessages.scrollToPosition(chatMessages.size - 1)
                message_input.text.clear()
                // Save the message to the database
                val newMessage = mapOf(
                    "Content" to message,
                    "SendId" to currentUserUid,
                    "RecvId" to targetUserUid,
                    "Time" to chatMessage.time,
                    "Type" to type,
                    "FileName" to fileName
                )
                Firebase.database.getReference("users").child(currentUserUid!!)
                    .child(targetUserUid).child("Messages").push().setValue(newMessage)
                if(currentUserUid == targetUserUid) {
                    return
                }
                //Save for target user
                Firebase.database.getReference("users").child(targetUserUid!!)
                    .child(currentUserUid).child("Messages").push().setValue(newMessage)
                var noti = non_encrypted_message
                if(type == "audio") {
                    noti = getString(R.string.Audio)
                }
                else if(type == "image") {
                    noti = getString(R.string.Image)
                }
                else if(type == "video") {
                    noti = getString(R.string.Video)
                }
                else if(type == "location") {
                    noti = getString(R.string.Location)
                }
                else if(type == "file") {
                    noti = getString(R.string.File)
                }
                messageController.newMessageFriend(targetUserUid, currentUserUid, noti )
            }
        }
        else {
            if (message.isNotEmpty()) {
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

                            message_input.text.clear()
                            val newMessage = mapOf(
                                "Content" to message,
                                "SendId" to currentUserUid,
                                "RecvId" to targetUserUid,
                                "Time" to System.currentTimeMillis(),
                                "Type" to type,
                                "FileName" to fileName
                            )
//                            database.push().setValue(newMessage)
                            val messageRef = Firebase.database.getReference("groups").child(targetUserUid).child("Messages")
                            messageRef.push().setValue(newMessage)
                            var noti = non_encrypted_message
                            if(type == "audio") {
                                noti = getString(R.string.Audio)
                            }
                            else if(type == "image") {
                                noti = getString(R.string.Image)
                            }
                            else if(type == "video") {
                                noti = getString(R.string.Video)
                            }
                            else if(type == "location") {
                                noti = getString(R.string.Location)
                            }
                            else if(type == "file") {
                                noti = getString(R.string.File)
                            }
                            messageController.newMessageGroup(targetUserUid, currentUserUid, noti)
                            //messageController.newMessageGroup(targetUserUid, currentUserUid, message)
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
                val fileSize = contentResolver.openFileDescriptor(selectedMediaUri, "r")?.statSize ?: 0
                if (fileSize > 15 * 1024 * 1024) { // 15 MB in bytes
                    Toast.makeText(this, "File size exceeds 15MB", Toast.LENGTH_SHORT).show()
                } else {
                    uploadMediaToFirebase(selectedMediaUri)
                }
            }
        }
    }

    fun compressImage(fileUri: Uri, context: Context): ByteArray {
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // Adjust quality as needed
        return outputStream.toByteArray()
    }
    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
    private fun uploadMediaToFirebase(fileUri: Uri, takePicture : Boolean = false) {
        // Xác định loại MIME của tệp
        val mimeType = contentResolver.getType(fileUri)
        Log.d("MainChat", "MIME type: $mimeType")
        val fileName = getFileNameFromUri(fileUri)
        val storageRef = storage.reference.child("media/${UUID.randomUUID()}")

        val uploadTask = if (mimeType?.startsWith("image/") == true || takePicture == true) {
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_LONG).show()
            val compressedImage = compressImage(fileUri, this)
            storageRef.putBytes(compressedImage)
        } else if( mimeType?.startsWith("video/") == true) {
            sendSkeletonMess("video")
            storageRef.putFile(fileUri)
        }
        else {
            sendSkeletonMess("file")
            storageRef.putFile(fileUri)
        }

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                // Kiểm tra loại tệp và gửi tin nhắn tương ứng
                when {
                    mimeType?.startsWith("image/") == true || takePicture -> {
                        sendMessage(downloadUrl, "image")
                    }
                    mimeType?.startsWith("video/") == true -> {
                        sendMessage(downloadUrl, "video")
                    }
                    else -> {
                        sendMessage(downloadUrl, "file",fileName!!)
                    }
                }
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

    private fun sendSkeletonMess(type : String){
        val chatMessage = ChatMessage(
            content = "",
            sendId = currentUserUid ?: "",
            recvId = targetUserUid,
            //   status = false,
            time = System.currentTimeMillis(),
            isSent = false,
            type = type
        )
        chatMessages.add(chatMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerViewMessages.scrollToPosition(chatMessages.size - 1)
    }

    private fun stopRecordingAndSave() {
        sendSkeletonMess("audio")
        mediaRecorder?.apply {
            Log.d("MainChat", "Stopping recording")
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
        wavFilePath?.let {
            val audioFileUri = Uri.fromFile(File(it))
            val audioRef = storage.reference.child("audio/${UUID.randomUUID()}.wav")
            audioRef.putFile(audioFileUri)
                .addOnSuccessListener {
                    audioRef.downloadUrl.addOnSuccessListener { uri ->
                        sendMessage(uri.toString(), "audio")
                    }
                    Toast.makeText(this@MainChat, "Recording sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@MainChat, "Failed to send recording", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onResume() {
        super.onResume()
        checkBlockedStatus()
        createImageSeenRecycle()
    }

    private fun createImageSeenRecycle(){
        if(!isGroup){
            val ref = Firebase.firestore.collection("users").document(targetUserUid)
            ref.get().addOnSuccessListener { document ->
                val image = document["Avatar"] as String
                imageSeenList.add(ImageSeen(targetUserUid, image, false))
                seenAdapter.updateData(imageSeenList)
                listenSeen()
            }
        }
        else{
            val ref = Firebase.firestore.collection("groups").document(targetUserUid)
            ref.get().addOnSuccessListener { document ->
                val participants = document["Participants"] as List<String>
                var isListen = false
                for (participant in participants) {
                    if(participant != currentUserUid) {
                        Firebase.firestore.collection("users").document(participant).get().addOnSuccessListener { document ->
                            val image = document["Avatar"] as String
                            imageSeenList.add(ImageSeen(participant, image, false))
                            seenAdapter.updateData(imageSeenList)
                            if(!isListen)listenSeen()
                            isListen = true
                        }
                    }
                }

            }
        }
    }

    private fun listenSeen() {
        if (!isGroup) {
            // Khởi tạo listener và lưu vào biến
            userStatusListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.getValue(Boolean::class.java) ?: false
                    imageSeenList.find {
                        it.id == targetUserUid
                    }?.setSeenId(status)
                    seenAdapter.updateData(imageSeenList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý khi bị lỗi
                }
            }
            Firebase.database.getReference("users")
                .child(targetUserUid)
                .child(currentUserUid)
                .child("Status")
                .addValueEventListener(userStatusListener!!)

        } else {
            // Khởi tạo listener cho nhóm và lưu vào biến
            groupStatusListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val genericTypeIndicator = object : GenericTypeIndicator<Map<String, Boolean>>() {}
                    val status = snapshot.getValue(genericTypeIndicator)

                    if (status != null) {
                        for (participant in status.keys) {
                            if (participant != currentUserUid) {
                                imageSeenList.find {
                                    it.id == participant
                                }?.setSeenId(status[participant] ?: false)
                            }
                        }
                        seenAdapter.updateData(imageSeenList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", error.message)
                }
            }
            Firebase.database.getReference("groups")
                .child(targetUserUid)
                .child("Status")
                .addValueEventListener(groupStatusListener!!)
        }
    }

    private fun removeStatusListeners() {
        userStatusListener?.let {
            Firebase.database.getReference("users")
                .child(targetUserUid)
                .child(currentUserUid)
                .child("Status")
                .removeEventListener(it)
        }

        groupStatusListener?.let {
            Firebase.database.getReference("groups")
                .child(targetUserUid)
                .child("Status")
                .removeEventListener(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::blockListener.isInitialized) {
            blockListener.remove()
        }
        if (::chatAdapter.isInitialized) {
            chatAdapter.releaseResources()
        }
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
        val blockedMessage1 = findViewById<FrameLayout>(R.id.blockMsg1)
        val blockedMessage2 = findViewById<FrameLayout>(R.id.blockMsg2)


        val userId = auth.currentUser?.uid ?: return
        val targetUserUid = intent.getStringExtra("uid") ?: return

        val db = Firebase.firestore

        // Kiểm tra nếu A (người hiện tại) block B
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val blockedUsers = document["Blocks"] as? List<String>
                    if (blockedUsers != null) {
                        for (blockedUser in blockedUsers) {
                            val uid = blockedUser as? String
                            if (uid == targetUserUid) {
                                // A đã block B
                                isBlocked = true
                                inputBar.visibility = View.GONE
                                blockedMessage2.visibility = View.VISIBLE
                                videoCallBtn.visibility = View.GONE
                                callVoiceBtn.visibility = View.GONE
                                return@addOnSuccessListener
                            }
                        }
                    }
                }
                // Nếu A không block B, kiểm tra ngược lại
                db.collection("users").document(targetUserUid)
                    .get()
                    .addOnSuccessListener { targetDocument ->
                        if (targetDocument.exists()) {
                            val blockedByTarget = targetDocument["Blocks"] as? List<String>
                            if (blockedByTarget != null) {
                                for (blockedUser in blockedByTarget) {
                                    val uid = blockedUser as? String
                                    if (uid == userId) {
                                        // B đã block A
                                        isBlocked = true
                                        inputBar.visibility = View.GONE
                                        blockedMessage1.visibility = View.VISIBLE
                                        videoCallBtn.visibility = View.GONE
                                        callVoiceBtn.visibility = View.GONE
                                        return@addOnSuccessListener
                                    }
                                }
                            }
                        }
                        // Nếu không có ai block ai, hiển thị giao diện bình thường
                        isBlocked = false
                        inputBar.visibility = View.VISIBLE
                        blockedMessage1.visibility = View.GONE
                        blockedMessage2.visibility = View.GONE
                        videoCallBtn.visibility = View.VISIBLE
                        callVoiceBtn.visibility = View.VISIBLE
                    }
                    .addOnFailureListener {
                        // Lỗi khi kiểm tra B block A
                        inputBar.visibility = View.VISIBLE
                        blockedMessage1.visibility = View.GONE
                        blockedMessage2.visibility = View.GONE
                    }
            }
            .addOnFailureListener {
                // Lỗi khi kiểm tra A block B
                inputBar.visibility = View.VISIBLE
                blockedMessage1.visibility = View.GONE
                blockedMessage2.visibility = View.GONE
            }
    }

    override fun onMessageLongClick(position: Int, message: ChatMessage) {
        // Xử lý khi người dùng long click vào tin nhắn
        showOptionsDialog(position, message)
    }
    private fun showOptionsDialog(position: Int, message: ChatMessage) {
        // Inflate custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_options, null)

        // Find views in the dialog layout
        val pinButton: Button = dialogView.findViewById(R.id.pin_button)
        val deleteButton: Button = dialogView.findViewById(R.id.delete_button)
        val cancelButton: Button = dialogView.findViewById(R.id.cancel_button)

        if(isGroup){
            Firebase.database.getReference("groups").child(targetUserUid).child("Messages").child(message.chatId)
                .child("Pinned").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val isPinned = snapshot.getValue(Boolean::class.java) ?: false
                        if(isPinned){
                            pinButton.text = getString(R.string.unpin)
                        }
                        else {
                            pinButton.text = getString(R.string.pin)
                        }
                    }
                    else {
                        pinButton.text = getString(R.string.pin)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                }
            })
        }
        else{
            Firebase.database.getReference("users").child(currentUserUid)
                .child(targetUserUid).child("Messages").child(message.chatId).child("Pinned")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val isPinned = snapshot.getValue(Boolean::class.java) ?: false
                            if (isPinned) {
                                pinButton.text = getString(R.string.unpin)
                            } else {
                                pinButton.text = getString(R.string.pin)
                            }
                        } else {
                            pinButton.text = getString(R.string.pin)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle possible errors.
                    }
                })
        }
        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Set button actions
        pinButton.setOnClickListener {
            val messageId = message.chatId
            if (messageId.isNotEmpty()) {
                if (!message.pinned) {
                    // Pin the message
                    pinMessage(message, messageId)
                } else {
                    // Unpin the message
                    unpinMessage(message, messageId)
                }
            }
            dialog.dismiss()
        }

        deleteButton.setOnClickListener {
            // Delete the message
            deleteMessage(position)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            // Dismiss the dialog
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Show the dialog
        dialog.show()
    }

    // Helper function to pin the message
    private fun pinMessage(message: ChatMessage, messageId: String) {
        if (!isGroup) {
            // Pin message for private chat
            Firebase.database.getReference("users").child(currentUserUid)
                .child(targetUserUid).child("Messages").child(messageId).child("Pinned")
                .setValue(true)

            Firebase.database.getReference("users").child(targetUserUid)
                .child(currentUserUid).child("Messages").child(messageId).child("Pinned")
                .setValue(true)

            lifecycleScope.launch {
                try {
                    val name = fetchName(message.sendId)
                    val newMessage = mapOf(
                        "Content" to message.content,
                        "Name" to name,
                    )
                    Firebase.database.getReference("users").child(currentUserUid)
                        .child(targetUserUid).child("PinnedMessages").child(messageId)
                        .setValue(newMessage)

                    Firebase.database.getReference("users").child(targetUserUid)
                        .child(currentUserUid).child("PinnedMessages").child(messageId)
                        .setValue(newMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // Pin message for group chat
            Firebase.database.getReference("groups").child(targetUserUid).child("Messages").child(messageId)
                .child("Pinned").setValue(true)

            lifecycleScope.launch {
                try {
                    val name = fetchName(message.sendId)
                    val newMessage = mapOf(
                        "Content" to message.content,
                        "Name" to name,
                    )
                    Firebase.database.getReference("groups").child(targetUserUid).child("PinnedMessages").child(messageId)
                        .setValue(newMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Helper function to unpin the message
    private fun unpinMessage(message: ChatMessage, messageId: String) {
        if (!isGroup) {
            // Unpin message for private chat
            Firebase.database.getReference("users").child(currentUserUid)
                .child(targetUserUid).child("Messages").child(messageId).child("Pinned")
                .setValue(false)

            Firebase.database.getReference("users").child(targetUserUid)
                .child(currentUserUid).child("Messages").child(messageId).child("Pinned")
                .setValue(false)

            Firebase.database.getReference("users").child(currentUserUid)
                .child(targetUserUid).child("PinnedMessages").orderByChild("Content").equalTo(message.content)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            child.ref.removeValue()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

            Firebase.database.getReference("users").child(targetUserUid)
                .child(currentUserUid).child("PinnedMessages").orderByChild("Content").equalTo(message.content)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            child.ref.removeValue()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            // Unpin message for group chat
            Firebase.database.getReference("groups").child(targetUserUid).child("Messages").child(messageId)
                .child("Pinned").setValue(false)

            Firebase.database.getReference("groups").child(targetUserUid).child("PinnedMessages").orderByChild("Content").equalTo(message.content)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            child.ref.removeValue()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    suspend fun fetchName(sendId: String): String {
        val document = Firebase.firestore.collection("users").document(sendId).get().await()
        return if (document.exists()) {
            document["Name"] as String
        } else {
            "Unknown"
        }
    }

    override fun onPause() {
        super.onPause()
        removeStatusListeners()
    }

    private fun deleteMessage(position: Int) {
        // xóa tin nhắn trong firebase
        val message = chatMessages[position]
        val messageId = message.chatId
        if (messageId.isNotEmpty()) {
            if (!isGroup) {
                Firebase.database.getReference("users").child(currentUserUid)
                    .child(targetUserUid).child("Messages").child(messageId).removeValue()
                Firebase.database.getReference("users").child(targetUserUid)
                    .child(currentUserUid).child("Messages").child(messageId).removeValue()
            } else {
                Firebase.database.getReference("groups").child(targetUserUid).child("Messages")
                    .child(messageId).removeValue()
            }
        }

        chatMessages.removeAt(position)
        recyclerViewMessages.adapter?.notifyItemRemoved(position)
    }

    private fun addBlockListener() {
        val userId = auth.currentUser?.uid ?: return
        val targetUserUid = intent.getStringExtra("uid") ?: return

        val db = Firebase.firestore

        // Listen for changes in the current user's Blocks field
        blockListener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MainChat", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    checkBlockedStatus()
                }
            }

        // Listen for changes in the target user's Blocks field
        blockListener = db.collection("users").document(targetUserUid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MainChat", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    checkBlockedStatus()
                }
            }
    }



}