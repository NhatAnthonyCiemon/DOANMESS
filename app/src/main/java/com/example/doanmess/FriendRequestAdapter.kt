package com.example.doanmess

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class FriendRequestAdapter(private val requests: MutableList<FriendRequestModel>) : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {

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

        // Xử lý sự kiện cho nút Accept
        holder.btnAccept.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                requests.removeAt(pos) // Xóa yêu cầu kết bạn tại vị trí 'pos'
                notifyItemRemoved(pos) // Cập nhật RecyclerView

                // Kiểm tra xem danh sách có rỗng không và cập nhật giao diện nếu cần
                if (requests.isEmpty()) {
                    Toast.makeText(holder.itemView.context, "No more friend requests.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Xử lý sự kiện cho nút Reject
        holder.btnReject.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                requests.removeAt(pos) // Xóa yêu cầu kết bạn tại vị trí 'pos'
                notifyItemRemoved(pos) // Cập nhật RecyclerView

                // Kiểm tra xem danh sách có rỗng không và cập nhật giao diện nếu cần
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