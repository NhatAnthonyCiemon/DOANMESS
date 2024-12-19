package com.example.doanmess.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.doanmess.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class ListViewPinnedActivity : HandleOnlineActivity() {
    val idMess = mutableListOf<String>()
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
                            idMess.add(child.key.toString())
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
                            showUnpinDialog(selectedMessage, idMess[position])
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListViewPinnedActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        val imgBack = findViewById<Button>(R.id.imgBack)
        imgBack.setOnClickListener {
            finish() // Close the current activity and go back to the previous one
        }
    }

    // Hàm hiển thị AlertDialog để bỏ ghim
    private fun showUnpinDialog(message: Map<String, String>, chatId: String) {
        val content = message["Content"] ?: "Undefine"
        val name = message["Name"] ?: "Undefine"

        val messageText = getString(R.string.unpinned_message_content)
            .replace("{name}", name)
            .replace("{content}", content)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.unpinned_message_title))
            .setMessage(messageText)
            .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                unpinMessage(message, chatId)
            }
            .setNegativeButton(getString(R.string.no)) { dialog, which ->
                dialog.dismiss()
            }
            .create().apply {
                // Thiết lập background cho dialog
                window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
            }
            .show()
    }

    // Hàm xử lý bỏ ghim tin nhắn
    // Hàm xử lý bỏ ghim tin nhắn
    private fun unpinMessage(message: Map<String, String>, chatId: String) {
        val contentToUnpin = message["Content"] ?: return // Lấy nội dung tin nhắn cần bỏ ghim
        val nameToUnpin = message["Name"] ?: return // Lấy tên người gửi tin nhắn cần bỏ ghim
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val targetUserUid = intent.getStringExtra("targetUserUid") ?: return

        // Tham chiếu đến database
        val database = FirebaseDatabase.getInstance()

        // Kiểm tra và xử lý chiều của currentUserUid và targetUserUid
        val groupRefCurrentToTarget = database.getReference("groups")
            .child(targetUserUid)
            .child("PinnedMessages")

        Firebase.database.getReference("groups").child(targetUserUid).child("Messages").child(chatId)
            .child("Pinned").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.ref.setValue(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListViewPinnedActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

        val userRefCurrentToTarget = database.getReference("users")
            .child(currentUserUid)
            .child(targetUserUid)
            .child("PinnedMessages")

        Firebase.database.getReference("users").child(currentUserUid)
            .child(targetUserUid).child("Messages").child(chatId).child("Pinned").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.ref.setValue(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListViewPinnedActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })


        val userRefTargetToCurrent = database.getReference("users")
            .child(targetUserUid)
            .child(currentUserUid)
            .child("PinnedMessages")

        Firebase.database.getReference("users").child(targetUserUid)
            .child(currentUserUid).child("Messages").child(chatId).child("Pinned").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.ref.setValue(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListViewPinnedActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

        // Kiểm tra từng nhóm dữ liệu để tìm và xóa tin nhắn
        val databaseRefList = listOf(groupRefCurrentToTarget, userRefCurrentToTarget, userRefTargetToCurrent)

        for (databaseRef in databaseRefList) {
            databaseRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    for (child in snapshot.children) {
                        val pinnedMessage = child.value as? Map<String, String>
                        if (pinnedMessage != null) {
                            // Kiểm tra cả nội dung và tên
                            val content = pinnedMessage["Content"]
                            val name = pinnedMessage["Name"]

                            if (content == contentToUnpin && name == nameToUnpin) {
                                // Xóa tin nhắn có cả nội dung và tên trùng khớp
                                child.ref.removeValue()
                                break
                            }
                        }
                    }
                }
            }
        }

        Toast.makeText(this@ListViewPinnedActivity, "Unpinned message sucessfull.", Toast.LENGTH_SHORT).show()

        // Sau khi xóa xong, lấy lại dữ liệu để cập nhật
        refreshPinnedMessages()
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
                    idMess.clear()
                    for (child in snapshot.children) {
                        val message = child.value as? Map<String, String>
                        if (message != null) {
                            pinnedMessages.add(message)
                            idMess.add(child.key.toString())
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
                            showUnpinDialog(selectedMessage, idMess[position])
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
