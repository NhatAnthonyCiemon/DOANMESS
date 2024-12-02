package com.example.doanmess

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class SeenAdapter(private var imageSeenList: List<ImageSeen>) : RecyclerView.Adapter<SeenAdapter.SeenViewHolder>() {

    class SeenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgAva)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_seen, parent, false)
        return SeenViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeenViewHolder, position: Int) {
        val imageSeen = imageSeenList[position]
        Picasso.get()
            .load(imageSeen.image)
            .resize(20, 20)  // Điều chỉnh kích thước ảnh phù hợp với ImageView nếu cần
            .centerCrop()  // Cắt ảnh để phù hợp với ImageView
            .networkPolicy(NetworkPolicy.OFFLINE)  // Kiểm tra bộ nhớ cache trước, nếu có
            .into(holder.imageView, object : Callback {
                override fun onSuccess() {
                    // Nếu ảnh đã có trong bộ nhớ cache, không cần tải lại từ mạng
                }

                override fun onError(e: Exception?) {
                    Picasso.get()
                        .load(imageSeen.image)
                        .resize(20, 20)
                        .centerCrop()
                        .into(holder.imageView)
                }
            })
        holder.imageView.visibility = if (imageSeen.seen) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return imageSeenList.size
    }

    fun updateData(newImageSeenList: List<ImageSeen>) {
        imageSeenList = newImageSeenList
        notifyDataSetChanged()
    }
}