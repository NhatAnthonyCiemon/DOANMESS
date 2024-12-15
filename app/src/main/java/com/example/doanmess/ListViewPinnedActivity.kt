package com.example.doanmess

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class ListViewPinnedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_view_pinned)
        val currentUserUid = intent.getStringExtra("currentUserUid") ?: return
        val targetUserUid = intent.getStringExtra("targetUserUid") ?: return

        // Gán ListView và hiển thị dữ liệu
        val listView: ListView = findViewById(R.id.listViewPinned)

        // Truy xuất dữ liệu từ Firebase
        val database = FirebaseDatabase.getInstance()
        val groupRef = database.getReference("groups")
            .child(targetUserUid)
            .child("PinnedMessages")

        groupRef.get().addOnCompleteListener { groupTask ->
            val databaseRef = if (groupTask.isSuccessful && groupTask.result.exists()) {
                groupRef
            } else {
                database.getReference("users")
                    .child(currentUserUid)
                    .child(targetUserUid)
                    .child("PinnedMessages")
            }

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pinnedMessages = mutableListOf<Map<String, String>>()

                    for (child in snapshot.children) {
                        val message = child.value as? Map<String, String>
                        if (message != null) {
                            pinnedMessages.add(message)
                        }
                    }

                    // Biến đổi danh sách các tin nhắn thành chuỗi có thể hiển thị
                    val messagesList = pinnedMessages.map { message ->
                        val content = message["Content"] ?: "Nội dung không xác định"
                        val name = message["Name"] ?: "Tên không xác định"
                        "$name: $content"
                    }

                    // Sử dụng ArrayAdapter để hiển thị tin nhắn trong ListView
                    val adapter = ArrayAdapter(this@ListViewPinnedActivity, android.R.layout.simple_list_item_1, messagesList)
                    listView.adapter = adapter

                    // Xử lý sự kiện khi người dùng nhấn vào một mục
                    listView.setOnItemClickListener { parent, view, position, id ->
                        pinnedMessages[position]?.let { selectedMessage ->
                            showUnpinDialog(selectedMessage)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListViewPinnedActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        val imgBack = findViewById<ImageView>(R.id.imgBack)
        imgBack.setOnClickListener {
            finish() // Close the current activity and go back to the previous one
        }
    }

    // Hàm hiển thị AlertDialog để bỏ ghim
    private fun showUnpinDialog(message: Map<String, String>) {
        val content = message["Content"] ?: "Nội dung không xác định"
        val name = message["Name"] ?: "Tên không xác định"

        AlertDialog.Builder(this)
            .setTitle("Bỏ ghim tin nhắn")
            .setMessage("Bạn có chắc chắn muốn bỏ ghim tin nhắn:\n$name: $content?")
            .setPositiveButton("Có") { dialog, which ->
                unpinMessage(message)
            }
            .setNegativeButton("Không") { dialog, which ->
                dialog.dismiss()
            }
            .create().apply {
                // Thiết lập background cho dialog
                window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
            }
            .show()
    }

    // Hàm xử lý bỏ ghim tin nhắn
    private fun unpinMessage(message: Map<String, String>) {
        val contentToUnpin = message["Content"] ?: return // Lấy nội dung tin nhắn cần bỏ ghim
        val nameToUnpin = message["Name"] ?: return // Lấy tên người gửi tin nhắn cần bỏ ghim
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val targetUserUid = intent.getStringExtra("targetUserUid") ?: return

        // Tham chiếu đến database
        val database = FirebaseDatabase.getInstance()
        val groupRef = database.getReference("groups")
            .child(targetUserUid)
            .child("PinnedMessages")

        val databaseRef = if (groupRef.get().isSuccessful) {
            // Nếu tồn tại trong "groups"
            groupRef
        } else {
            // Nếu không tồn tại trong "groups", sử dụng "users"
            database.getReference("users")
                .child(currentUserUid)
                .child(targetUserUid)
                .child("PinnedMessages")
        }

        // Lấy dữ liệu từ databaseRef, xóa tin nhắn và cập nhật
        databaseRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                var isRemoved = false

                for (child in snapshot.children) {
                    val pinnedMessage = child.value as? Map<String, String>
                    if (pinnedMessage != null) {
                        // Kiểm tra cả nội dung và tên
                        val content = pinnedMessage["Content"]
                        val name = pinnedMessage["Name"]

                        if (content == contentToUnpin && name == nameToUnpin) {
                            // Xóa tin nhắn có cả nội dung và tên trùng khớp
                            child.ref.removeValue()
                            isRemoved = true
                            break
                        }
                    }
                }

                if (isRemoved) {
                    Toast.makeText(this@ListViewPinnedActivity, "Đã bỏ ghim tin nhắn.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ListViewPinnedActivity, "Không tìm thấy tin nhắn để bỏ ghim.", Toast.LENGTH_SHORT).show()
                }

                // Sau khi xóa xong, lấy lại dữ liệu để cập nhật
                refreshPinnedMessages()
            } else {
                Toast.makeText(this@ListViewPinnedActivity, "Lỗi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun refreshPinnedMessages() {
        val listView: ListView = findViewById(R.id.listViewPinned)
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val targetUserUid = intent.getStringExtra("targetUserUid") ?: return

        // Tham chiếu lại đến database để lấy danh sách mới nhất
        val database = FirebaseDatabase.getInstance()
        val groupRef = database.getReference("groups")
            .child(targetUserUid)
            .child("PinnedMessages")

        groupRef.get().addOnCompleteListener { groupTask ->
            val databaseRef = if (groupTask.isSuccessful && groupTask.result.exists()) {
                groupRef
            } else {
                database.getReference("users")
                    .child(currentUserUid)
                    .child(targetUserUid)
                    .child("PinnedMessages")
            }

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pinnedMessages = mutableListOf<Map<String, String>>()

                    for (child in snapshot.children) {
                        val message = child.value as? Map<String, String>
                        if (message != null) {
                            pinnedMessages.add(message)
                        }
                    }

                    // Biến đổi danh sách các tin nhắn thành chuỗi có thể hiển thị
                    val messagesList = pinnedMessages.map { message ->
                        val content = message["Content"] ?: "Nội dung không xác định"
                        val name = message["Name"] ?: "Tên không xác định"
                        "$name: $content"
                    }

                    // Sử dụng ArrayAdapter để hiển thị tin nhắn trong ListView
                    val adapter = ArrayAdapter(
                        this@ListViewPinnedActivity,
                        android.R.layout.simple_list_item_1,
                        messagesList
                    )
                    listView.adapter = adapter

                    // Xử lý sự kiện khi người dùng nhấn vào một mục
                    listView.setOnItemClickListener { parent, view, position, id ->
                        pinnedMessages[position]?.let { selectedMessage ->
                            showUnpinDialog(selectedMessage)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ListViewPinnedActivity,
                        "Lỗi: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
        }
}
