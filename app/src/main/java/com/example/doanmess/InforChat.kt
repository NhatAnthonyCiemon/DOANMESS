package com.example.doanmess

import HandleOnlineActivity
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class InforChat : HandleOnlineActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_infor_chat)
        val imgBack = findViewById<ImageView>(R.id.imgBack)
        imgBack.setOnClickListener {
            finish() // Close the current activity and go back to the previous one
        }

        val chatUserId = intent.getStringExtra("uid") // Retrieve the uid from the intent

        val imgView = findViewById<ImageView>(R.id.imgView) // Avatar ImageView
        val txtName = findViewById<TextView>(R.id.txtName) // Name TextView
//        if (!chatUserId.isNullOrEmpty()) {
//            // Fetch user data from Firebase Firestore
//            val firestore = FirebaseFirestore.getInstance()
//            firestore.collection("users").document(chatUserId).get()
//                .addOnSuccessListener { document ->
//                    if (document != null) {
//                        // Retrieve the avatar URL and name from the document
//                        val avatarUrl = document.getString("Avatar")
//                        val name = document.getString("Name")
//
//                        // Set the name in the TextView
//                        txtName.text = name ?: "User"
//
//                        // Load the avatar into the ImageView using Glide
//                        if (!avatarUrl.isNullOrEmpty()) {
//                            Glide.with(this).load(avatarUrl).into(imgView)
//                        } else {
//                            imgView.setImageResource(R.drawable.ic_launcher_background) // Placeholder
//                        }
//                    } else {
//                        Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(this, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        } else {
//            Toast.makeText(this, "Invalid user ID.", Toast.LENGTH_SHORT).show()
//        }

        if (!chatUserId.isNullOrEmpty()) {
            val firestore = FirebaseFirestore.getInstance()

            // Tìm trong collection "users"
            firestore.collection("users").document(chatUserId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Lấy thông tin từ "users"
                        val avatarUrl = document.getString("Avatar")
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


        // Thiết lập onClickListener cho các FrameLayout

//        frmNotice.setOnClickListener {
//            // Thay đổi màu nền của frmNotice
//            changeBackgroundColor(frmNotice, "#D9D9D9", 150)
//
//            // Tạo và hiển thị hộp thoại AlertDialog
//            AlertDialog.Builder(this)
//                .setTitle("Unnoticed")
//                .setMessage("Do you want to unnoticed?")
//                .setPositiveButton("Yes") { dialog, which ->
//                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
//                    if (currentUserId != null && !chatUserId.isNullOrEmpty()) {
//                        val firestore = FirebaseFirestore.getInstance()
//                        firestore.collection("users").document(currentUserId).get()
//                            .addOnSuccessListener { document ->
//                                // Lấy danh sách người dùng bị unnotice của người dùng hiện tại
//                                val unnoticedUsers = document["Unnoticed"] as? List<Map<String, Any>>
//                                val isAlreadyUnnoticed = unnoticedUsers?.any { it["uid"] == chatUserId } ?: false
//
//                                // Kiểm tra nếu người chưa đã bị unnoticed
//                                if (!isAlreadyUnnoticed) {
//                                    firestore.collection("users").document(currentUserId)
//                                        .update("Unnoticed", FieldValue.arrayUnion(mapOf("uid" to chatUserId, "timeStamp" to System.currentTimeMillis())))
//                                        .addOnSuccessListener {
//                                            Toast.makeText(this, "User unnoticed successfully.", Toast.LENGTH_SHORT).show()
//                                        }
//                                        .addOnFailureListener { e ->
//                                            Toast.makeText(this, "Failed to unnoticed user: ${e.message}", Toast.LENGTH_SHORT).show()
//                                        }
//                                } else {
//                                    // Nếu người dùng đã bị unnoticed
//                                    Toast.makeText(this, "User is unnoticed.", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                            .addOnFailureListener { e ->
//                                Toast.makeText(this, "Failed to check unnoticed users: ${e.message}", Toast.LENGTH_SHORT).show()
//                            }
//                    } else {
//                        Toast.makeText(this, "Invalid user data.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                .setNegativeButton("No") { dialog, which ->
//                    // Đóng hộp thoại khi người dùng chọn "No"
//                    dialog.dismiss()
//                }
//                .show()
//        }

//        frmNotice.setOnClickListener {
//            // Thay đổi màu nền của frmNotice
//            changeBackgroundColor(frmNotice, "#D9D9D9", 150)
//
//            // Tạo và hiển thị hộp thoại AlertDialog
//            AlertDialog.Builder(this)
//                .setTitle("Unnoticed")
//                .setMessage("Do you want to unnoticed?")
//                .setPositiveButton("Yes") { dialog, which ->
//                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
//                    if (currentUserId != null && !chatUserId.isNullOrEmpty()) {
//                        val firestore = FirebaseFirestore.getInstance()
//                        firestore.collection("users").document(currentUserId).get()
//                            .addOnSuccessListener { document ->
//                                // Lấy danh sách người dùng bị unnoticed của người dùng hiện tại
//                                val unnoticedUsers = document["Unnoticed"] as? List<String>
//                                val isAlreadyUnnoticed = unnoticedUsers?.contains(chatUserId) ?: false
//
//                                // Kiểm tra nếu người dùng chưa bị unnoticed
//                                if (!isAlreadyUnnoticed) {
//                                    firestore.collection("users").document(currentUserId)
//                                        .update("Unnoticed", FieldValue.arrayUnion(chatUserId))
//                                        .addOnSuccessListener {
//                                            Toast.makeText(this, "User unnoticed successfully.", Toast.LENGTH_SHORT).show()
//                                        }
//                                        .addOnFailureListener { e ->
//                                            Toast.makeText(this, "Failed to unnoticed user: ${e.message}", Toast.LENGTH_SHORT).show()
//                                        }
//                                } else {
//                                    // Nếu người dùng đã bị unnoticed
//                                    Toast.makeText(this, "User is already unnoticed.", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                            .addOnFailureListener { e ->
//                                Toast.makeText(this, "Failed to check unnoticed users: ${e.message}", Toast.LENGTH_SHORT).show()
//                            }
//                    } else {
//                        Toast.makeText(this, "Invalid user data.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                .setNegativeButton("No") { dialog, which ->
//                    // Đóng hộp thoại khi người dùng chọn "No"
//                    dialog.dismiss()
//                }
//                .show()
//        }

        frmNotice.setOnClickListener {
            // Thay đổi màu nền của frmNotice
            changeBackgroundColor(frmNotice, "#D9D9D9", 150)

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
                                // Lấy danh sách người dùng bị "Unnoticed" của người dùng hiện tại
                                val unnoticedUsers = document["Unnoticed"] as? List<String>
                                val isAlreadyUnnoticed = unnoticedUsers?.contains(chatUserId) ?: false

                                if (isAlreadyUnnoticed) {
                                    // Nếu người dùng đã có trong danh sách "Unnoticed", xóa họ ra
                                    firestore.collection("users").document(currentUserId)
                                        .update("Unnoticed", FieldValue.arrayRemove(chatUserId))
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "User removed from unnoticed list.", Toast.LENGTH_SHORT).show()
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
                .show()
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
                    // Thực hiện thao tác block ở đây
                }
                .setNegativeButton("No") { dialog, which ->
                    // Đóng hộp thoại khi người dùng chọn "Không"
                    dialog.dismiss()
                }
                .show()
        }
        frmBlock.setOnClickListener {
            changeBackgroundColor(frmBlock, "#D9D9D9", 150)
            AlertDialog.Builder(this)
                .setTitle("Block")
                .setMessage("Do you want to block?")
                .setPositiveButton("Yes") { dialog, which ->
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                    if (currentUserId != null && !chatUserId.isNullOrEmpty()) {
                        val firestore = FirebaseFirestore.getInstance()
                        firestore.collection("users").document(currentUserId).get()
                            .addOnSuccessListener { document ->
                                val blockedUsers = document["Blocks"] as? List<Map<String, Any>>
                                val isAlreadyBlocked = blockedUsers?.any { it["uid"] == chatUserId } ?: false

                                if (!isAlreadyBlocked) {
                                    firestore.collection("users").document(currentUserId)
                                        .update("Blocks", FieldValue.arrayUnion(mapOf("uid" to chatUserId, "timeStamp" to System.currentTimeMillis())))
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "User blocked successfully.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Failed to block user: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(this, "User is already blocked.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to check blocked users: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
        frmTrash.setOnClickListener {
            changeBackgroundColor(frmTrash, "#D9D9D9", 150)
            AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Do you want to delete?")
                .setPositiveButton("Yes") { dialog, which ->
                    // Xử lý khi người dùng chọn "Có"
                    // Thực hiện thao tác block ở đây
                }
                .setNegativeButton("No") { dialog, which ->
                    // Đóng hộp thoại khi người dùng chọn "Không"
                    dialog.dismiss()
                }
                .show()
        }


    }

    private fun changeBackgroundColor(view: View, color: String, duration: Long) {
        val background = view.background
        val currentColor = if (background is ColorDrawable) (background as ColorDrawable).color else Color.TRANSPARENT

        view.setBackgroundColor(Color.parseColor(color))
        view.postDelayed({ view.setBackgroundColor(currentColor) }, duration)
    }

//    private fun fetchPinnedMessages() {
//        // Lấy UID của người dùng hiện tại và người dùng mục tiêu
//        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        val targetUserUid = intent.getStringExtra("uid") ?: return
//
//        // Truy cập vào nhánh PinnedMessages
//        val isGroup = intent.getBooleanExtra("isGroup", false)
//        val databaseRef = if (isGroup) {
//            Firebase.database.getReference("groups").child(targetUserUid).child("PinnedMessages")
//        } else {
//            Firebase.database.getReference("users").child(currentUserUid).child(targetUserUid).child("PinnedMessages")
//        }
//
//        // Lắng nghe dữ liệu
//        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val pinnedMessages = mutableListOf<Map<String, String>>()
//
//                // Lặp qua tất cả các tin nhắn ghim
//                for (child in snapshot.children) {
//                    val message = child.value as? Map<String, String>
//                    if (message != null) {
//                        pinnedMessages.add(message)
//                    }
//                }
//
//                // Chuyển dữ liệu tin nhắn ghim sang Intent và chuyển sang ListViewPinnedActivity
//                val intent = Intent(this@InforChat, ListViewPinnedActivity::class.java)
//                val bundle = Bundle()
//                bundle.putSerializable("pinnedMessages", ArrayList(pinnedMessages)) // Chuyển đổi sang ArrayList vì Serializable yêu cầu
//                intent.putExtras(bundle)
//
////                Toast.makeText(this@InforChat, ArrayList(pinnedMessages).toString(), Toast.LENGTH_LONG).show()
//                startActivity(intent)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Xử lý lỗi nếu có
//                Toast.makeText(this@InforChat, "Không thể lấy dữ liệu.", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

    private fun fetchPinnedMessages() {
        // Lấy UID của người dùng hiện tại và người dùng mục tiêu
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val targetUserUid = intent.getStringExtra("uid") ?: return

        // Tham chiếu đến database
        val database = Firebase.database
        val groupRef = database.getReference("groups")
            .child(targetUserUid)
            .child("PinnedMessages")

        groupRef.get().addOnCompleteListener { groupTask ->
            val databaseRef = if (groupTask.isSuccessful && groupTask.result.exists()) {
                // Nếu tồn tại trong "groups"
                groupRef
            } else {
                // Nếu không tồn tại trong "groups", sử dụng "users"
                database.getReference("users")
                    .child(currentUserUid)
                    .child(targetUserUid)
                    .child("PinnedMessages")
            }

            // Lắng nghe dữ liệu từ databaseRef
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pinnedMessages = mutableListOf<Map<String, String>>()

                    // Lặp qua tất cả các tin nhắn ghim
                    for (child in snapshot.children) {
                        val message = child.value as? Map<String, String>
                        if (message != null) {
                            pinnedMessages.add(message)
                        }
                    }

                    // Chuyển dữ liệu tin nhắn ghim sang Intent và chuyển sang ListViewPinnedActivity
                    val intent = Intent(this@InforChat, ListViewPinnedActivity::class.java)
                    val bundle = Bundle()
                    bundle.putSerializable("pinnedMessages", ArrayList(pinnedMessages)) // Chuyển đổi sang ArrayList vì Serializable yêu cầu
                    intent.putExtras(bundle)

                    startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi nếu có
                    Toast.makeText(this@InforChat, "Không thể lấy dữ liệu.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


}