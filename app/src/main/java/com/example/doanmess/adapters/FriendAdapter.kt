package com.example.doanmess.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doanmess.R
import com.example.doanmess.models.Friend
import com.squareup.picasso.Picasso

class FriendAdapter (
    private var list: MutableList<Friend>, private val onItemClicked: (String, Boolean) -> Unit) : RecyclerView.Adapter<FriendAdapter.MyViewHolder>() {

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
            if(list[position].reqFriend == true) {
                return 1
            } else {
                return 0
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            if(viewType == 1) {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.cancel_request_item, parent,false)
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

     //   holder.imgView.setImageResource(list[position].image)
        Picasso
            .get()
            .load(list[position].image)
            .into(holder.imgView);
        holder.addBtn.setOnClickListener{
            list[position].reqFriend = !list[position].reqFriend
            onItemClicked(list[position].id, list[position].reqFriend)
            notifyDataSetChanged()
        }
    }

    fun changeList(newList: MutableList<Friend>) {
        list = newList
        notifyDataSetChanged()
    }

}