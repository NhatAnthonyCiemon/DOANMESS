package com.example.doanmess

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

class ContactsAdapter(var cont: Activity,var contactList:  List<Contact>) : RecyclerView.Adapter<ContactsAdapter.MyViewHolder>() {
    var onItemClick: ((Contact) -> Unit)? = null

    inner class MyViewHolder : RecyclerView.ViewHolder {
        var imgView : ImageView
        var nameView : TextView
        var onlineDot: View

        constructor(itemView: View) : super(itemView) {
        }
        init {
            imgView = itemView.findViewById(R.id.userImage)
            nameView = itemView.findViewById(R.id.userName)
            onlineDot = itemView.findViewById(R.id.onlineStatus)
            itemView.setOnClickListener() {
                onItemClick?.invoke(contactList[adapterPosition])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent,false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.nameView.text = contactList[position].name
        if(contactList[position].avatar == "") {
            holder.imgView.setImageResource(R.drawable.avatar_placeholder_allchat)
            return
        }
        Picasso.get()
            .load(contactList[position].avatar)
            .resize(100, 100)  // Điều chỉnh kích thước ảnh phù hợp với ImageView
            .centerCrop()
            .networkPolicy(NetworkPolicy.OFFLINE)  // Kiểm tra bộ nhớ cache trước, nếu có
            .into(holder.imgView, object : Callback {
                override fun onSuccess() {
                    // Nếu ảnh đã được tải từ cache, sẽ không tải lại từ mạng
                }

                override fun onError(e: Exception?) {
                    // Nếu ảnh chưa có trong cache, tải từ mạng
                    Picasso.get()
                        .load(contactList[position].avatar)
                        .resize(100, 100)
                        .centerCrop()
                        .into(holder.imgView)
                }
            })
        if (contactList[position].online) {
            holder.onlineDot.visibility = View.VISIBLE
        } else {
            holder.onlineDot.visibility = View.INVISIBLE
        }
    }

    fun changeList(filteredList: List<Contact>) {
        contactList = filteredList
        notifyDataSetChanged()
    }

}