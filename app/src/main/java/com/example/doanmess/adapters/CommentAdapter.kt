package com.example.doanmess.adapters
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doanmess.R
import com.example.doanmess.models.Comment

class CommentAdapter(private val commentList: List<Comment>,
                     private val onLikeClick: (Comment) -> Unit,
                     private val onUserImageClick: (Comment) -> Unit) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        val likeButton: TextView = itemView.findViewById(R.id.likeButton)
        val commentLikes: TextView = itemView.findViewById(R.id.commentLikes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        // Bind data to the views
        holder.usernameTextView.text = comment.username
        holder.contentTextView.text = comment.content
        holder.timestampTextView.text = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", comment.time)
        holder.commentLikes.text = comment.likes.size.toString()
        Glide.with(holder.userImage.context).load(comment.profilePic).into(holder.userImage)
        // Set text style to bold if the comment is liked
        if (comment.liked) {
            holder.likeButton.setTypeface(null, Typeface.BOLD)
            holder.likeButton.setTextColor(ContextCompat.getColor(holder.likeButton.context, R.color.liked_color))
            holder.commentLikes.setTextColor(ContextCompat.getColor(holder.commentLikes.context, R.color.liked_color))
        } else {
            holder.likeButton.setTypeface(null, Typeface.NORMAL)
            holder.likeButton.setTextColor(Color.BLACK)
            holder.commentLikes.setTextColor(Color.BLACK)
        }
        holder.likeButton.setOnClickListener {
            onLikeClick(comment)
            if (comment.liked) {
                holder.likeButton.setTypeface(null, Typeface.BOLD)
                holder.likeButton.setTextColor(ContextCompat.getColor(holder.likeButton.context, R.color.liked_color))
                holder.commentLikes.setTextColor(ContextCompat.getColor(holder.commentLikes.context, R.color.liked_color))
            } else {
                holder.likeButton.setTypeface(null, Typeface.NORMAL)
                holder.likeButton.setTextColor(Color.BLACK)
                holder.commentLikes.setTextColor(Color.BLACK)
            }
            holder.commentLikes.text = comment.likes.size.toString()
        }
        holder.userImage.setOnClickListener {
            onUserImageClick(comment)
        }
    }

    override fun getItemCount() = commentList.size
}