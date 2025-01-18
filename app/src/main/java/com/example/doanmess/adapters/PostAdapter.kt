package com.example.doanmess.adapters

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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doanmess.R
import com.example.doanmess.models.Post

class PostAdapter(
    private val postList: List<Post>,
    private val onLikeClick: (Post) -> Unit,
    private val onUserImageClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit,
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postTitle: TextView = itemView.findViewById(R.id.postTitle)
        val imagePreview: ImageView = itemView.findViewById(R.id.imagePreview)
        val imageMessageView : ImageView = itemView.findViewById(R.id.imageMessageView)
        val videoPlayBtn : ImageButton= itemView.findViewById(R.id.videoPlayBtn)
        val videoPreview : PlayerView = itemView.findViewById(R.id.videoPreview)
        val cardVideo : CardView = itemView.findViewById(R.id.cardVideo)
        val postLikes: TextView = itemView.findViewById(R.id.postLikes)
        val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val username: TextView = itemView.findViewById(R.id.userName)
        val time: TextView = itemView.findViewById(R.id.postTime)
        val toggleButton: Button = itemView.findViewById(R.id.toggleButton)
        val commentButton : ImageButton = itemView.findViewById(R.id.commentButton)
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
            holder.cardVideo.visibility = View.GONE
            holder.imageMessageView.visibility = View.GONE
            holder.videoPlayBtn.visibility = View.GONE
            return
        }
        //load image or video based on the media type
        if(post.type.contains("mp4")){
            Log.d("PostAdapter", "onBindViewHolder: video")
            holder.videoPlayBtn.visibility = View.VISIBLE
            holder.cardVideo.visibility = View.VISIBLE
            holder.imageMessageView.visibility = View.VISIBLE // Hiển thị thumbnail trước
            holder.imagePreview.visibility = View.GONE
            holder.videoPreview.visibility = View.GONE
            // Show a placeholder while loading the thumbnail
            Glide.with(holder.imageMessageView.context)
                .load(post.mediaFile) // Use video URL as a placeholder for the thumbnail
                .placeholder(R.drawable.black_image) // Placeholder image
                .error(R.drawable.black_image) // Fallback image in case of error
                .into(holder.imageMessageView)
            // Set click listener to start video playback
            holder.videoPreview.player = null
            holder.cardVideo.setOnClickListener{
                setUpVideoPlayer(post.id, post.mediaFile, holder)
            }
            holder.videoPlayBtn.setOnClickListener {
                setUpVideoPlayer(post.id, post.mediaFile, holder)
            }

        }else if(post.type.contains("image")){
            holder.videoPlayBtn.visibility = View.GONE
            holder.imageMessageView.visibility = View.GONE
            holder.videoPreview.visibility = View.GONE
            holder.cardVideo.visibility = View.GONE
            holder.imagePreview.visibility = View.VISIBLE
            Glide.with(holder.imagePreview.context).load(post.mediaFile).into(holder.imagePreview)
        }
        holder.commentButton.setOnClickListener {
            onCommentClick(post)
        }
    }
    val audioPlayerLists : MutableMap<String, ExoPlayer> = mutableMapOf()
    fun setUpVideoPlayer(postId:String, content: String, holder : PostViewHolder){
        holder.imageMessageView.visibility = View.GONE
        holder.videoPlayBtn.visibility = View.GONE
        holder.videoPreview.visibility = View.VISIBLE
        if(!audioPlayerLists.containsKey(postId)){
            val tmp = ExoPlayer.Builder(holder.cardVideo.context).build()
            audioPlayerLists[postId] = tmp
            // Thiết lập video URL
            val mediaItem = MediaItem.fromUri(content)
            tmp!!.setMediaItem(mediaItem)
        }
        val tmp = audioPlayerLists[postId]
        holder.videoPreview.player = tmp
        tmp!!.prepare()
        holder.videoPreview.player!!.playWhenReady = true
    }
    public fun releaseAllPlayers(){
        for((_, player) in audioPlayerLists){
            player.release()
        }
        audioPlayerLists.clear()
    }

    override fun getItemCount() = postList.size

}