package com.example.doanmess

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupAddedAdapter(
    private var list: MutableList<GroupAdded>, private val onItemClicked: (Int) -> Unit) : RecyclerView.Adapter<GroupAddedAdapter.MyViewHolder>() {

    inner class MyViewHolder : RecyclerView.ViewHolder {
        var nameView : TextView
        var deleteBtn: ImageButton

        constructor(itemView: View) : super(itemView) {
        }
        init{
            nameView = itemView.findViewById(R.id.userName)
            deleteBtn = itemView.findViewById(R.id.deleteBtn)
            deleteBtn.setOnClickListener({
                onItemClicked(adapterPosition)
                notifyDataSetChanged()
            })
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.added_name, parent,false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.nameView.setText(list[position].name)
/*        holder.nameView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )*/
    }


}