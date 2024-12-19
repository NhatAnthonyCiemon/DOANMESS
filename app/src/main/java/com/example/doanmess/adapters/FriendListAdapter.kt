package com.example.doanmess.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.request.RequestOptions
import com.example.doanmess.models.FriendModel
import com.example.doanmess.R

class FriendListAdapter(
    private val friendLists: MutableList<FriendModel>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_block, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = friendLists[position]
        holder.userName.text = item.name

        val requestOptions = RequestOptions().circleCrop()
        val avatarUrl = if (item.avatar.isNotEmpty()) item.avatar else "https://firebasestorage.googleapis.com/v0/b/doan-cb428.appspot.com/o/avatars%2F3a1a9f11-a045-4072-85da-7202c9bc9989.jpg?alt=media&token=4f3a7b0d-7c87-443f-9e1d-4222f8d22bb9"
        Glide.with(holder.itemView.context)
            .load(avatarUrl)
            .apply(requestOptions)
            .into(holder.profileImage)


//        // Nút xóa tin nhắn và hủy block
//        holder.btnDelete.setOnClickListener {
//            val pos = holder.adapterPosition
//            if (pos != RecyclerView.NO_POSITION) {
//                AlertDialog.Builder(holder.itemView.context)
//                    .setTitle("Unfriend")
//                    .setMessage("Do you want to unfriend this user?")
//                    .setPositiveButton("Yes") { dialog, _ ->
//                        val uid1 = FirebaseAuth.getInstance().currentUser?.uid // Người dùng hiện tại
//                        val uid2 = friendLists[pos].uid // Bạn bị xóa
//
//                        if (uid1 != null && uid2 != null) {
//                            val firestore = FirebaseFirestore.getInstance()
//                            val database = FirebaseDatabase.getInstance().reference
//
//                            // Xóa tin nhắn giữa hai người
//                            database.child("users").child(uid1).child(uid2).removeValue()
//                                .addOnSuccessListener {
//                                    database.child("users").child(uid2).child(uid1).removeValue()
//                                        .addOnSuccessListener {
//                                            // Xóa bạn khỏi danh sách bạn bè trong Firestore
//                                            firestore.collection("users").document(uid1)
//                                                .update("Friends", FieldValue.arrayRemove(uid2))
//                                                .addOnSuccessListener {
//                                                    firestore.collection("users").document(uid2)
//                                                        .update("Friends", FieldValue.arrayRemove(uid1))
//                                                        .addOnSuccessListener {
//                                                            // Xóa khỏi danh sách hiển thị
//                                                            friendLists.removeAt(pos)
//                                                            notifyItemRemoved(pos)
//
//                                                            if (friendLists.isEmpty()) {
//                                                                Toast.makeText(
//                                                                    holder.itemView.context,
//                                                                    "No more friends.",
//                                                                    Toast.LENGTH_SHORT
//                                                                ).show()
//                                                            }
//                                                        }
//                                                        .addOnFailureListener { e ->
//                                                            Toast.makeText(
//                                                                holder.itemView.context,
//                                                                "Error removing friend: ${e.message}",
//                                                                Toast.LENGTH_SHORT
//                                                            ).show()
//                                                        }
//                                                }
//                                                .addOnFailureListener { e ->
//                                                    Toast.makeText(
//                                                        holder.itemView.context,
//                                                        "Error removing friend: ${e.message}",
//                                                        Toast.LENGTH_SHORT
//                                                    ).show()
//                                                }
//                                        }
//                                        .addOnFailureListener { e ->
//                                            Toast.makeText(
//                                                holder.itemView.context,
//                                                "Failed to delete chat for $uid2: ${e.message}",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                }
//                                .addOnFailureListener { e ->
//                                    Toast.makeText(
//                                        holder.itemView.context,
//                                        "Failed to delete chat for $uid1: ${e.message}",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                        } else {
//                            Toast.makeText(
//                                holder.itemView.context,
//                                "Unable to find user information.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                    .setNegativeButton("No") { dialog, _ ->
//                        dialog.dismiss()
//                    }
//                    .show()
//            }
//        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Unfriend")
                    .setMessage("Do you want to unfriend this user?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        val uid1 = FirebaseAuth.getInstance().currentUser?.uid // Người dùng hiện tại
                        val uid2 = friendLists[pos].uid // Bạn bị xóa

                        if (uid1 != null && uid2 != null) {
                            val firestore = FirebaseFirestore.getInstance()

                            // Xóa bạn khỏi danh sách bạn bè trong Firestore
                            firestore.collection("users").document(uid1)
                                .update("Friends", FieldValue.arrayRemove(uid2))
                                .addOnSuccessListener {
                                    firestore.collection("users").document(uid2)
                                        .update("Friends", FieldValue.arrayRemove(uid1))
                                        .addOnSuccessListener {
                                            // Xóa khỏi danh sách hiển thị
                                            friendLists.removeAt(pos)
                                            notifyItemRemoved(pos)

                                            if (friendLists.isEmpty()) {
                                                Toast.makeText(
                                                    holder.itemView.context,
                                                    "No more friends.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                holder.itemView.context,
                                                "Error removing friend: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Error removing friend: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(
                                holder.itemView.context,
                                "Unable to find user information.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create().apply {
                        // Thiết lập background cho dialog
                        window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
                    }
                    .show()
            }
        }


    }

    override fun getItemCount(): Int {
        return friendLists.size
    }
}

