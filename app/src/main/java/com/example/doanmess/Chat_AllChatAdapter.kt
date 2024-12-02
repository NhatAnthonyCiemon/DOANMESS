package com.example.doanmess
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class Chat_AllChatAdapter(private val cont: Activity, private val list: List<DataMess>) : RecyclerView.Adapter<Chat_AllChatAdapter.MessHolder>() {

    inner class MessHolder(itemView: View, clickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
                notifyItemChanged(adapterPosition) // Update only the clicked item
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
        return if (item.status) 0 else 1
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
            val imgOffNotification = findViewById<ImageView>(R.id.imgOffNotification)
            txtName.text = item.name
            txtContent.text = item.message
            txtTime.text = item.time
            imgOffNotification.visibility = if (item.isNotify) View.GONE else View.VISIBLE
            //Picasso.get().load(item.avatar).into(imgAvatar)
            Picasso.get()
                .load(item.avatar)
                .resize(100, 100)  // Điều chỉnh kích thước ảnh phù hợp với ImageView
                .centerCrop()
                .networkPolicy(NetworkPolicy.OFFLINE)  // Kiểm tra bộ nhớ cache trước, nếu có
                .into(imgAvatar, object : Callback {
                    override fun onSuccess() {
                        // Nếu ảnh đã được tải từ cache, sẽ không tải lại từ mạng
                    }

                    override fun onError(e: Exception?) {
                        // Nếu ảnh chưa có trong cache, tải từ mạng
                        Picasso.get()
                            .load(item.avatar)
                            .resize(100, 100)
                            .centerCrop()
                            .into(imgAvatar)
                    }
                })
            // Use lifecycleScope to run coroutine
//            (cont as? LifecycleOwner)?.lifecycleScope?.launch {
//                try {
//                    val imageLoader = ImageLoader(cont)
//                    val path = imageLoader.checkFile(item.avatar!!, item.uid)
//                    if (path != item.avatar && File(path).exists()) {
//                        Picasso.get().load(File(path)).into(imgAvatar)
//                    } else {
//                        Picasso.get().load(item.avatar).into(imgAvatar)
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }


            if (item is DataMessGroup) {
                txtName.text = item.groupname
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}

