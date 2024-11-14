package com.example.doanmess

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
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
        val videoFrame : FrameLayout = itemView.findViewById(R.id.video_frame)
        val playBtn : ImageButton = itemView.findViewById(R.id.play_button)
        val toggleButton: Button = itemView.findViewById(R.id.toggleButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        Log.d("PostActivity", "onBindViewHolder: $post")
        holder.username.text = post.username
        Glide.with(holder.imagePreview.context).load(post.profilePic).into(holder.userImage)
        //change timestamp to human readable time
        holder.time.text = android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", post.time)
        holder.postTitle.text = post.title
        // Handle "See more" and "Show less" functionality
        holder.postTitle.post {
            if (holder.postTitle.lineCount > 2) {
                holder.postTitle.maxLines = 2
                holder.toggleButton.visibility = View.VISIBLE
                holder.toggleButton.setOnClickListener {
                    if (holder.toggleButton.text == "See more") {
                        holder.postTitle.maxLines = Int.MAX_VALUE
                        holder.toggleButton.text = "Show less"
                    } else {
                        holder.postTitle.maxLines = 2
                        holder.toggleButton.text = "See more"
                    }
                }
            } else {
                holder.toggleButton.visibility = View.GONE
            }
        }
        holder.postLikes.text = post.likes.toString()
        holder.likeButton.setBackgroundResource(if (post.liked) R.drawable.heart_yellow else R.drawable.heart)
        holder.likeButton.setOnClickListener {
            onLikeClick(post)
            holder.likeButton.setBackgroundResource(if (post.liked) R.drawable.heart_yellow else R.drawable.heart)
            holder.postLikes.text = post.likes.toString()
        }
        holder.userImage.setOnClickListener {
            onUserImageClick(post)
        }
        if(post.mediaFile==""){
            holder.imagePreview.visibility = View.GONE
            holder.videoFrame.visibility = View.GONE
            holder.videoPreview.visibility = View.GONE
            holder.playBtn.visibility = View.GONE
            return
        }
        //load image or video based on the media type
        if(post.type.contains("mp4")){
            Log.d("PostAdapter", "onBindViewHolder: video")
            holder.videoFrame.visibility = View.VISIBLE
            holder.imagePreview.visibility = View.GONE
            holder.videoPreview.visibility = View.VISIBLE
            holder.playBtn.visibility = View.VISIBLE
            holder.videoPreview.setVideoPath(post.mediaFile)
            holder.videoPreview.setOnClickListener {
                if(holder.videoPreview.isPlaying){
                    holder.videoPreview.pause()
                    holder.playBtn.visibility = View.VISIBLE
                }else{
                    holder.videoPreview.start()
                    holder.playBtn.visibility = View.GONE
                }
            }
            holder.playBtn.setOnClickListener {
                holder.videoPreview.start()
                holder.playBtn.visibility = View.GONE
            }
        }else if(post.type.contains("image")){
            holder.imagePreview.visibility = View.VISIBLE
            holder.videoFrame.visibility = View.GONE
            holder.videoPreview.visibility = View.GONE
            holder.playBtn.visibility = View.GONE
            Glide.with(holder.imagePreview.context).load(post.mediaFile).into(holder.imagePreview)
        }

    }
    override fun getItemCount() = postList.size

}