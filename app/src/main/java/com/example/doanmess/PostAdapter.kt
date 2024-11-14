package com.example.doanmess

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostAdapter(
    private val postList: List<Post>,
    private val onLikeClick: (Post) -> Unit,
    private val onUserImageClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postTitle: TextView = itemView.findViewById(R.id.postTitle)
        val imagePreview: ImageView = itemView.findViewById(R.id.imagePreview)
        val videoPreview : VideoView = itemView.findViewById(R.id.videoPreview)
        val postLikes: TextView = itemView.findViewById(R.id.postLikes)
        val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val username: TextView = itemView.findViewById(R.id.userName)
        val time: TextView = itemView.findViewById(R.id.postTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.username.text = post.username
        Glide.with(holder.imagePreview.context).load(post.profilePic).into(holder.userImage)
        //change timestamp to human readable time
        holder.time.text = android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", post.time)
        holder.postTitle.text = post.title
        holder.postLikes.text = post.likes.toString()
        holder.likeButton.setBackgroundResource(if (post.liked) R.drawable.heart_yellow else R.drawable.heart)
        //load image or video based on the media type
        if(post.type.contains("mp4")){
            Log.d("PostAdapter", "onBindViewHolder: video")
            holder.imagePreview.visibility = View.GONE
            holder.videoPreview.visibility = View.VISIBLE
            holder.videoPreview.setVideoPath(post.mediaFile)
            holder.videoPreview.setOnPreparedListener {
                val mediaController = MediaController(holder.videoPreview.context)
                holder.videoPreview.setMediaController(mediaController)
                holder.videoPreview.requestFocus()
                mediaController.setAnchorView(holder.videoPreview)
                holder.videoPreview.start()
            }
        }else if(post.type.contains("image")){
            holder.imagePreview.visibility = View.VISIBLE
            holder.videoPreview.visibility = View.GONE
            Glide.with(holder.imagePreview.context).load(post.mediaFile).into(holder.imagePreview)
        }
        holder.likeButton.setOnClickListener {
            onLikeClick(post)
            holder.likeButton.setBackgroundResource(if (post.liked) R.drawable.heart_yellow else R.drawable.heart)
            holder.postLikes.text = post.likes.toString()
        }
        holder.userImage.setOnClickListener {
            onUserImageClick(post)
        }
    }
    override fun getItemCount() = postList.size

}