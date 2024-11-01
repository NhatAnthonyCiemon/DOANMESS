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
import com.squareup.picasso.MemoryPolicy
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

    class MyViewHolder : RecyclerView.ViewHolder {
        var imgView : ImageView
        var nameView : TextView
        var onlineDot: View

        constructor(itemView: View) : super(itemView) {
        }
        init{
            imgView = itemView.findViewById(R.id.userImage)
            nameView = itemView.findViewById(R.id.userName)
            onlineDot = itemView.findViewById(R.id.onlineStatus)
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
        holder.nameView.setText(contactList[position].name)
        (cont as? LifecycleOwner)?.lifecycleScope?.launch {
            try {
                val ImageLoader = ImageLoader(cont)
                val path = ImageLoader.checkFile(contactList[position].avatar, contactList[position].id)
                if(path != contactList[position].avatar && File(path).exists()) {
                    Picasso.get().load(File(path)).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.imgView)
                }
                else {
                    Picasso.get().load(contactList[position].avatar).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.imgView)
                }
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
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