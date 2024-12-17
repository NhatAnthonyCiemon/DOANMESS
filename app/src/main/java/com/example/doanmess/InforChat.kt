package com.example.doanmess


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson


class InforChat : HandleOnlineActivity() {
    private var isBlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_infor_chat)
        val imgBack = findViewById<ImageView>(R.id.imgBack)
        imgBack.setOnClickListener {
            finish() // Close the current activity and go back to the previous one
        }

        val chatUserId = intent.getStringExtra("uid") // Retrieve the uid from the intent
        val isGroup = intent.getBooleanExtra("isGroup", false)
        isBlocked = intent.getBooleanExtra("isBlocked", false)

        val btnCall = findViewById<FloatingActionButton>(R.id.btnCall)
        val btnVideo = findViewById<FloatingActionButton>(R.id.btnVideo)
        val btnInfo  = findViewById<FloatingActionButton>(R.id.btnInfor)

        checkBlock(isBlocked)

        btnCall.setOnClickListener {
            var intent = Intent(this, Call::class.java)
            if(isGroup){
                intent = Intent(this, CallGroup::class.java)
                intent.putExtra("groupId", chatUserId)
            }
            else{
                intent.putExtra("friendId", chatUserId)
            }
            intent.putExtra("call", true)
            intent.putExtra("isVideoCall", false)
            startActivity(intent)
            if(isGroup){
                MessageController().callVoiceGroup(chatUserId!!, FirebaseAuth.getInstance().currentUser?.uid!!)
            }
            else {
                MessageController().callVoiceFriend(chatUserId!!, FirebaseAuth.getInstance().currentUser?.uid!!)
            }
        }
        btnVideo.setOnClickListener {
            var intent = Intent(this, Call::class.java)
            if(isGroup){
                intent = Intent(this, CallGroup::class.java)
                intent.putExtra("groupId", chatUserId)
            }
            else{
                intent.putExtra("friendId", chatUserId)
            }
            intent.putExtra("call", true)
            intent.putExtra("isVideoCall", true)
            startActivity(intent)
            if(isGroup){
                MessageController().callVideoGroup(chatUserId!!, FirebaseAuth.getInstance().currentUser?.uid!!)
            }
            else {
                MessageController().callVideoFriend(chatUserId!!, FirebaseAuth.getInstance().currentUser?.uid!!)
            }
        }
        var avatarUrl : String ="";
        btnInfo.setOnClickListener{
            val intent = Intent(this, IndividualPost::class.java)
            intent.putExtra("current", FirebaseAuth.getInstance().currentUser?.uid)
            intent.putExtra("target", chatUserId)
            intent.putExtra("avatar", avatarUrl)
            startActivity(intent)
        }
        val imgView = findViewById<ImageView>(R.id.imgView) // Avatar ImageView
        val txtName = findViewById<TextView>(R.id.txtName) // Name TextView


        if (!chatUserId.isNullOrEmpty()) {
            val firestore = FirebaseFirestore.getInstance()

            // Tìm trong collection "users"
            firestore.collection("users").document(chatUserId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Lấy thông tin từ "users"
                        avatarUrl = document.getString("Avatar")!!
                        val name = document.getString("Name")

                        // Hiển thị thông tin
                        txtName.text = name ?: "User"
                        if (!avatarUrl.isNullOrEmpty()) {
                            Glide.with(this).load(avatarUrl).into(imgView)
                        } else {
                            imgView.setImageResource(R.drawable.ic_launcher_background) // Placeholder
                        }
                    } else {
                        // Nếu không tìm thấy trong "users", tiếp tục tìm trong "groups"
                        firestore.collection("groups").document(chatUserId).get()
                            .addOnSuccessListener { groupDoc ->
                                if (groupDoc != null && groupDoc.exists()) {
                                    // Lấy thông tin từ "groups"
                                    val groupAvatarUrl = groupDoc.getString("Avatar")
                                    val groupName = groupDoc.getString("Name")

                                    // Hiển thị thông tin nhóm
                                    txtName.text = groupName ?: "Group"
                                    if (!groupAvatarUrl.isNullOrEmpty()) {
                                        Glide.with(this).load(groupAvatarUrl).into(imgView)
                                    } else {
                                        imgView.setImageResource(R.drawable.ic_launcher_background) // Placeholder
                                    }
                                } else {
                                    // Không tìm thấy trong cả "users" lẫn "groups"
                                    Toast.makeText(this, "No user or group found.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to load group data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Invalid user ID.", Toast.LENGTH_SHORT).show()
        }


        val frmNotice = findViewById<FrameLayout>(R.id.frmNotice)
        val frmLink = findViewById<FrameLayout>(R.id.frmLink)
        val frmLimit = findViewById<FrameLayout>(R.id.frmLimit)
        val frmBlock = findViewById<FrameLayout>(R.id.frmBlock)
        val frmTrash = findViewById<FrameLayout>(R.id.frmTrash)
        val btnNotice: FloatingActionButton = findViewById(R.id.btnNotice)

        frmNotice.setOnClickListener {
            // Thay đổi màu nền của frmNotice
            changeBackgroundColor(frmNotice, "#D9D9D9", 150)
            showUnnoticedDialog(chatUserId)
        }

        btnNotice.setOnClickListener {
            showUnnoticedDialog(chatUserId)
        }


        frmLink.setOnClickListener {
            changeBackgroundColor(frmLink, "#D9D9D9", 150)
            // Chuyển sang ListViewPinnedActivity
            fetchPinnedMessages()
        }

        frmLimit.setOnClickListener {
            changeBackgroundColor(frmLimit, "#D9D9D9", 150)
            AlertDialog.Builder(this)
                .setTitle("Limit")
                .setMessage("Do you want to limit?")
                .setPositiveButton("Yes") { dialog, which ->
                    // Xử lý khi người dùng chọn "Có"
                }
                .setNegativeButton("No") { dialog, which ->
                    // Đóng hộp thoại khi người dùng chọn "Không"
                    dialog.dismiss()
                }
                .create().apply {
                    // Thiết lập background cho dialog
                    window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
                }
                .show()
        }
        frmBlock.setOnClickListener {
            changeBackgroundColor(frmBlock, "#D9D9D9", 150)

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId != null && !chatUserId.isNullOrEmpty()) {
                val firestore = FirebaseFirestore.getInstance()

                // Kiểm tra nếu currentUserId nằm trong Blocks của chatUserId
                firestore.collection("users").document(chatUserId).get()
                    .addOnSuccessListener { document ->
                        val blockedUsers = document["Blocks"] as? List<String>
                        val isBlockedByChatUser = blockedUsers?.contains(currentUserId) ?: false

                        if (isBlockedByChatUser) {
                            AlertDialog.Builder(this)
                                .setTitle("Blocked")
                                .setMessage("You have been blocked by this user.")
                                .setPositiveButton("OK") { dialog, which ->
                                    dialog.dismiss()
                                }
                                .create().apply {
                                    // Thiết lập background cho dialog
                                    window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
                                }
                                .show()
                            return@addOnSuccessListener
                        }

                        // Nếu không bị block bởi chatUserId, tiếp tục xử lý kiểm tra block/unblock
                        firestore.collection("users").document(currentUserId).get()
                            .addOnSuccessListener { userDocument ->
                                val currentUserBlockedUsers = userDocument["Blocks"] as? List<String>
                                val isAlreadyBlocked = currentUserBlockedUsers?.contains(chatUserId) ?: false

                                if (isAlreadyBlocked) {
                                    AlertDialog.Builder(this)
                                        .setTitle("Unblock")
                                        .setMessage("Do you want to unblock?")
                                        .setPositiveButton("Yes") { dialog, which ->
                                            firestore.collection("users").document(currentUserId)
                                                .update("Blocks", FieldValue.arrayRemove(chatUserId))
                                                .addOnSuccessListener {
                                                    isBlocked = false
                                                    checkBlock(isBlocked)
                                                    Toast.makeText(this, "User unblocked successfully.", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(this, "Failed to unblock user: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .setNegativeButton("No") { dialog, which ->
                                            dialog.dismiss()
                                        }
                                        .create().apply {
                                            // Thiết lập background cho dialog
                                            window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
                                        }
                                        .show()
                                } else {
                                    AlertDialog.Builder(this)
                                        .setTitle("Block")
                                        .setMessage("Do you want to block?")
                                        .setPositiveButton("Yes") { dialog, which ->
                                            firestore.collection("users").document(currentUserId)
                                                .update("Blocks", FieldValue.arrayUnion(chatUserId))
                                                .addOnSuccessListener {
                                                    isBlocked = true
                                                    checkBlock(isBlocked)
                                                    Toast.makeText(this, "User blocked successfully.", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(this, "Failed to block user: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .setNegativeButton("No") { dialog, which ->
                                            dialog.dismiss()
                                        }
                                        .create().apply {
                                            // Thiết lập background cho dialog
                                            window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
                                        }
                                        .show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to check blocked users: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to check if you are blocked: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        frmTrash.setOnClickListener {
            changeBackgroundColor(frmTrash, "#D9D9D9", 150)
            AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Do you want to delete?")
                .setPositiveButton("Yes") { dialog, which ->
                    // Xử lý khi người dùng chọn "Có"
                }
                .setNegativeButton("No") { dialog, which ->
                    // Đóng hộp thoại khi người dùng chọn "Không"
                    dialog.dismiss()
                }
                .create().apply {
                    // Thiết lập background cho dialog
                    window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
                }
                .show()
        }


    }

    private fun checkBlock(isBlocked: Boolean) {
        val btnCall = findViewById<FloatingActionButton>(R.id.btnCall)
        val btnVideo = findViewById<FloatingActionButton>(R.id.btnVideo)
        if (isBlocked) {
            btnCall.visibility = View.GONE
            btnVideo.visibility = View.GONE
        } else {
            btnCall.visibility = View.VISIBLE
            btnVideo.visibility = View.VISIBLE
        }
    }

    private fun changeBackgroundColor(view: View, color: String, duration: Long) {
        val background = view.background
        val currentColor = if (background is ColorDrawable) (background as ColorDrawable).color else Color.TRANSPARENT

        view.setBackgroundColor(Color.parseColor(color))
        view.postDelayed({ view.setBackgroundColor(currentColor) }, duration)
    }

    private fun showUnnoticedDialog(chatUserId: String?) {
        // Tạo và hiển thị hộp thoại AlertDialog
        AlertDialog.Builder(this)
            .setTitle("Unnoticed")
            .setMessage("Do you want to toggle unnoticed status?")
            .setPositiveButton("Yes") { dialog, which ->
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null && !chatUserId.isNullOrEmpty()) {
                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection("users").document(currentUserId).get()
                        .addOnSuccessListener { document ->
                            // Lấy danh sách người dùng bị "Unnoticed" từ Firestore
                            val unnoticedUsers = document["Unnoticed"] as? List<String> ?: emptyList()
                            val isAlreadyUnnoticed = unnoticedUsers.contains(chatUserId)

                            // Lưu danh sách hiện tại vào SharedPreferences
                            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("UnnoticedUsers", Gson().toJson(unnoticedUsers))
                            editor.apply()

                            if (isAlreadyUnnoticed) {
                                // Nếu người dùng đã có trong danh sách "Unnoticed", xóa họ ra
                                firestore.collection("users").document(currentUserId)
                                    .update("Unnoticed", FieldValue.arrayRemove(chatUserId))
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "User removed from unnoticed list.", Toast.LENGTH_SHORT).show()

                                        // Cập nhật lại SharedPreferences
                                        val updatedList = unnoticedUsers.toMutableList()
                                        updatedList.remove(chatUserId)
                                        editor.putString("UnnoticedUsers", Gson().toJson(updatedList))
                                        editor.apply()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Failed to remove user: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // Nếu người dùng chưa có trong danh sách "Unnoticed", thêm họ vào
                                firestore.collection("users").document(currentUserId)
                                    .update("Unnoticed", FieldValue.arrayUnion(chatUserId))
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "User added to unnoticed list.", Toast.LENGTH_SHORT).show()

                                        // Cập nhật lại SharedPreferences
                                        val updatedList = unnoticedUsers.toMutableList()
                                        updatedList.add(chatUserId)
                                        editor.putString("UnnoticedUsers", Gson().toJson(updatedList))
                                        editor.apply()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Failed to add user: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to check unnoticed users: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Invalid user data.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No") { dialog, which ->
                // Đóng hộp thoại khi người dùng chọn "No"
                dialog.dismiss()
            }
            .create().apply {
                // Thiết lập background cho dialog
                window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
            }
            .show()
    }


    private fun fetchPinnedMessages() {
        // Lấy UID của người dùng hiện tại và người dùng mục tiêu
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val targetUserUid = intent.getStringExtra("uid") ?: return

        val intent = Intent(this@InforChat, ListViewPinnedActivity::class.java)
        intent.putExtra("currentUserUid", currentUserUid)
        intent.putExtra("targetUserUid", targetUserUid)
        startActivity(intent)

    }
}