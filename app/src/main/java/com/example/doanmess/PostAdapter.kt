package com.example.doanmess

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
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
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
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
        val videoPreview : PlayerView = itemView.findViewById(R.id.videoPreview)
        val postLikes: TextView = itemView.findViewById(R.id.postLikes)
        val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val username: TextView = itemView.findViewById(R.id.userName)
        val time: TextView = itemView.findViewById(R.id.postTime)
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
        val text = post.username + " "+ post.title
        //make the post title name bold and size smaller
        val spannableString = SpannableString(text)
        // Bold span
        val boldSpan = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(boldSpan, 0, post.username.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        // Resize span
        val sizeSpan = RelativeSizeSpan(0.95f) // 1.5 times the default size
        spannableString.setSpan(sizeSpan, 0, post.username.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.postTitle.text = spannableString
        // Handle "See more" and "Show less" functionality
        holder.postTitle.post {
            if (post.title.length > 100) {
                holder.postTitle.maxLines = 2
                holder.toggleButton.visibility = View.VISIBLE
                holder.toggleButton.text = "See more"
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
            holder.videoPreview.visibility = View.GONE
            return
        }
        //load image or video based on the media type
        if(post.type.contains("mp4")){
            Log.d("PostAdapter", "onBindViewHolder: video")
            holder.imagePreview.visibility = View.GONE
            holder.videoPreview.visibility = View.VISIBLE
            setUpVideoPlayer(post.id, holder.videoPreview.context, holder.videoPreview, post.mediaFile)

        }else if(post.type.contains("image")){
            holder.imagePreview.visibility = View.VISIBLE
            holder.videoPreview.visibility = View.GONE
            Glide.with(holder.imagePreview.context).load(post.mediaFile).into(holder.imagePreview)
        }

    }
    val audioPlayerLists : MutableMap<String, ExoPlayer> = mutableMapOf()
    fun setUpVideoPlayer(postId:String, context: Context, playerView: PlayerView, content: String){
        if(!audioPlayerLists.containsKey(postId)){
            val tmp = ExoPlayer.Builder(context).build()
            audioPlayerLists[postId] = tmp
            // Thiết lập video URL
            val mediaItem = MediaItem.fromUri(content)
            tmp!!.setMediaItem(mediaItem)
        }
        val tmp = audioPlayerLists[postId]
        tmp!!.prepare()
        // Khởi tạo ExoPlayer từ Media3
        playerView.player = tmp
    }
    public fun releaseAllPlayers(){
        for((_, player) in audioPlayerLists){
            player.release()
        }
        audioPlayerLists.clear()
    }
    override fun getItemCount() = postList.size

}