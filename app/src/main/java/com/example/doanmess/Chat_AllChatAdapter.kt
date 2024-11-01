package com.example.doanmess
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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


class Chat_AllChatAdapter(private val cont: Activity, private val list: List<DataMess>) : RecyclerView.Adapter<Chat_AllChatAdapter.MessHolder>() {

    inner class MessHolder(itemview: View, clickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemview) {
        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
                notifyDataSetChanged()
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

            // Sử dụng lifecycleScope để chạy coroutine
            (cont as? LifecycleOwner)?.lifecycleScope?.launch {
                try {
                    val path = checkFile(item.avatar, item.uid)
                    Picasso.get().load(File(path)).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imgAvatar)
                }
                catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            if (item is DataMessGroup) {
                txtName.text = item.groupname
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    val mutex = Mutex()
    private suspend fun checkFile(Path: String, uid: String): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            val file = File(cont.filesDir, "uid_avatar.json")
            val gson = Gson()
            val list: MutableMap<String, String> = if (file.exists()) {
                val bufferedReader = BufferedReader(FileReader(file))
                val type = object : TypeToken<MutableMap<String, String>>() {}.type
                gson.fromJson(bufferedReader, type)
            } else {
                mutableMapOf()
            }

            var res: String = ""
            if (list.containsKey(uid) && list[uid] == Path) {
                res = "${cont.filesDir}/$uid.jpg"
            } else {
                list[uid] = Path
                val json = gson.toJson(list)
                saveFile(file, json)
                downloadImage(Path, uid)
                res = "${cont.filesDir}/$uid.jpg"
            }
            return@withContext res
        }


    }

     fun downloadImage(urlPath: String, uid: String){
        val url = URL(urlPath)
        val connection = url.openConnection()
        connection.connect()

        val input: InputStream = connection.getInputStream()
        val output = FileOutputStream(File(cont.filesDir, "$uid.jpg"))
        copyStream(input, output)
    }

    fun copyStream(input: InputStream, output: OutputStream) {
        input.use { inputStream ->
            output.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

     fun saveFile(file: File, json: String) {
        FileWriter(file).use { it.write(json) }
    }
}

