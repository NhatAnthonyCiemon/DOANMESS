package com.example.doanmess

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class FriendRequestAdapter(private val requests: MutableList<FriendRequestModel>) : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val requestTime: TextView = itemView.findViewById(R.id.requestTime)
        val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_request, parent, false)
        return ViewHolder(view)


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.userName.text = request.name
        holder.requestTime.text = request.time
      //  holder.profileImage.setImageResource(request.profileImg)
        Picasso
            .get()
            .load(request.profileImg)
            .into(holder.profileImage);
        // Xử lý sự kiện cho nút Accept
        holder.btnAccept.setOnClickListener {
            val pos = holder.adapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                val targetUserId = request.userId
                // Update current user's Friends and Requests
                val currentUserRef = db.collection("users").document(currentUserId)
                currentUserRef.update("Friends", FieldValue.arrayUnion(targetUserId))
                currentUserRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val requests = document.get("Requests") as List<Map<String, Any>>
                        val requestToRemove = requests.find { it["userId"] == targetUserId }
                        if (requestToRemove != null) {
                            currentUserRef.update("Requests", FieldValue.arrayRemove(requestToRemove))
                                .addOnSuccessListener {
                                    MessageController().newFriendAccpet(targetUserId, currentUserId)
                                    Toast.makeText(holder.itemView.context, "Friend request removed successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(holder.itemView.context, "Failed to remove friend request", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
                // Update target user's Friends and RequestSent
                val targetUserRef = db.collection("users").document(targetUserId)
                targetUserRef.update("Friends", FieldValue.arrayUnion(currentUserId))
                targetUserRef.update("RequestSent", FieldValue.arrayRemove(currentUserId))
                // Remove the request from the list and notify the adapter
                requests.removeAt(pos)
                notifyItemRemoved(pos)

                // Check if the list is empty and show a toast if needed
                if (requests.isEmpty()) {
                    Toast.makeText(holder.itemView.context, "No more friend requests.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Xử lý sự kiện cho nút Reject
        holder.btnReject.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val targetUserId = request.userId

                // Update current user's Requests
                val currentUserRef = db.collection("users").document(currentUserId)
                currentUserRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val requests = document.get("Requests") as List<Map<String, Any>>
                        val requestToRemove = requests.find { it["userId"] == targetUserId }
                        if (requestToRemove != null) {
                            currentUserRef.update("Requests", FieldValue.arrayRemove(requestToRemove))
                                .addOnSuccessListener {
                                    Toast.makeText(holder.itemView.context, "Friend request removed successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(holder.itemView.context, "Failed to remove friend request", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }

                // Update target user's RequestSent
                val targetUserRef = db.collection("users").document(targetUserId)
                targetUserRef.update("RequestSent", FieldValue.arrayRemove(currentUserId))
                // Remove the request from the list and notify the adapter
                requests.removeAt(pos)
                notifyItemRemoved(pos)

                // Check if the list is empty and show a toast if needed
                if (requests.isEmpty()) {
                    Toast.makeText(holder.itemView.context, "No more friend requests.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return requests.size
    }
}