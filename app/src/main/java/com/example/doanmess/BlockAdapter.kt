package com.example.doanmess

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BlockAdapter(private val blockLists: MutableList<BlockModel>) : RecyclerView.Adapter<BlockAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val blockTime: TextView = itemView.findViewById(R.id.blockTime)
        val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_block, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = blockLists[position]
        holder.userName.text = item.name
        holder.blockTime.text = item.timestamp
        Glide.with(holder.itemView.context).load(item.avatar).into(holder.profileImage)

        holder.btnAccept.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                blockLists.removeAt(pos)
                notifyItemRemoved(pos)
                if (blockLists.isEmpty()) {
                    Toast.makeText(holder.itemView.context, "No more blocked users.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                blockLists.removeAt(pos)
                notifyItemRemoved(pos)
                if (blockLists.isEmpty()) {
                    Toast.makeText(holder.itemView.context, "No more blocked users.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return blockLists.size
    }
}