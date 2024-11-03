package com.example.doanmess

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class GroupAddAdapter(
    private val cont: Activity,
    private var list: MutableList<GroupAdd>, private val onItemClicked: (String, String) -> Unit) : RecyclerView.Adapter<GroupAddAdapter.MyViewHolder>() {

    inner class MyViewHolder : RecyclerView.ViewHolder {
        var imgView : ImageView
        var nameView : TextView
        var addBtn: LinearLayout

        constructor(itemView: View) : super(itemView) {
        }
        init{
            imgView = itemView.findViewById(R.id.userImage)
            nameView = itemView.findViewById(R.id.userName)
            addBtn = itemView.findViewById(R.id.addBtn)

        }

    }

    override fun getItemViewType(position: Int): Int {
        if(list[position].added == true) {
            return 1
        } else {
            return 0
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if(viewType == 1) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.added_item, parent,false)
            return MyViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.add_item, parent,false)
            return MyViewHolder(v)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.nameView.setText(list[position].name)

//
        (cont as? LifecycleOwner)?.lifecycleScope?.launch {
            try {
                val ImageLoader = ImageLoader(cont)
                val path =
                    ImageLoader.checkFile(list[position].image, list[position].id)
                if (path != list[position].image && File(path).exists()) {
                    Picasso.get().load(File(path)).memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(holder.imgView)
                } else {
                    Picasso.get().load(list[position].image)
                        .memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.imgView)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


        holder.addBtn.setOnClickListener({
            if(list[position].added == false) {
                list[position].added = true
                onItemClicked(list[position].id, list[position].name)
                notifyDataSetChanged()
            }


        })
    }

    fun changeList(newList: MutableList<GroupAdd>) {
        list = newList
        notifyDataSetChanged()
    }

}