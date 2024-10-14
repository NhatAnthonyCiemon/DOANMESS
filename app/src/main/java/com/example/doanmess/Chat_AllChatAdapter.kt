package com.example.doanmess
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater


class Chat_AllChatAdapter(var list: List<DataMess>): RecyclerView.Adapter<Chat_AllChatAdapter.MessHolder>() {
    inner class MessHolder(itemview: View,clickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemview) {
        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }
    }
    private lateinit var listener: OnItemClickListener
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    override fun getItemViewType(position: Int): Int {
        val item = list[position]
        return if (item.status) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = if (viewType == 1) {
            layoutInflater.inflate(R.layout.chat_all_chat_noseen, parent, false)
        } else {
            layoutInflater.inflate(R.layout.chat_all_chat, parent, false)
        }
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return MessHolder(view, listener)
    }

    override fun onBindViewHolder(holder: MessHolder, position: Int) {
       holder.itemView.apply {
            val item = list[position]
            val txtName = findViewById<TextView>(R.id.txtName)
            val txtContent = findViewById<TextView>(R.id.txtContent)
            val txtTime = findViewById<TextView>(R.id.txtTime)
            val imgAvatar = findViewById<ImageView>(R.id.imgAvatar)
            txtName.text = item.name
            txtContent.text = item.message
            txtTime.text = item.time
            imgAvatar.setImageResource(item.avatar)
       }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}