package com.example.createuiproject
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doanmess.MainChat
import com.example.doanmess.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class ChatAdapter(private val chatMessages: MutableList<MainChat.ChatMessage>, val isGroup: Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var auth = Firebase.auth
    private var currentUser = auth.currentUser

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_WITH_AVATAR = 3
        private const val VIEW_TYPE_NO_AVATAR = 4
    }

    // ViewHolder class
    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView) // Your TextView for the message content
        val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView) // Your TextView for the sender's name
        val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView) // Your ImageView for the avatar

        // Additional views for sent messages can be added here
        val timeStampTextView: TextView = itemView.findViewById(R.id.timestampTextView) // Your TextView for the sent message
    }

    // Last sender's ID
    private var lastSenderId: String =""

    override fun getItemViewType(position: Int): Int {
        val currentMessage = chatMessages[position]
        if (position == 0) {
            lastSenderId = ""
        }

        // Check if the current message is sent by the current user
        if (currentMessage.sendId == currentUser?.uid) {
            lastSenderId = currentMessage.sendId
            return VIEW_TYPE_SENT
        } else if (isGroup) {
            // Check if this message's sender ID is the same as the last message's sender ID
            return if (currentMessage.sendId == lastSenderId) {
                VIEW_TYPE_NO_AVATAR // Same sender, no avatar
            } else {
                // Different sender, update lastSenderId
                lastSenderId = currentMessage.sendId
                VIEW_TYPE_WITH_AVATAR // New sender, show avatar and name
            }
        } else {
            return VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            VIEW_TYPE_WITH_AVATAR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.group_message_with_avatar, parent, false)
                MessageWithAvatarViewHolder(view)
            }
            VIEW_TYPE_NO_AVATAR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.group_message_with_no_avatar, parent, false)
                MessageNoAvatarViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatMessages[position]

        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is MessageWithAvatarViewHolder -> holder.bind(message)
            is MessageNoAvatarViewHolder -> holder.bind(message)
        }
    }
    private var mediaPlayer: MediaPlayer? = null
    private var currenAudio = ""
    private fun setupAudioPlayer(audioPlayerView: View, progressBar: ProgressBar, audioUrl: String) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            try {
                handler.post {
                    audioPlayerView.setOnClickListener {
                        if(currenAudio != audioUrl){
                            mediaPlayer?.release()
                            mediaPlayer = null
                            currenAudio = audioUrl
                            mediaPlayer = MediaPlayer()
                            mediaPlayer?.setDataSource(audioUrl)
                            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                            mediaPlayer?.prepare()

                        }
                        if (mediaPlayer?.isPlaying == true) {
                            mediaPlayer?.pause()
                        } else {
                            mediaPlayer?.start()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error playing audio: $e")
                handler.post {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }
    fun releaseResources() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
    override fun getItemCount(): Int {
        return chatMessages.size
    }

    // ViewHolder for sent messages
    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val audioPlayerView: ImageView = itemView.findViewById(R.id.audioPlayerView)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.audioProgressBar)
        private val audioPlayerLayout : ConstraintLayout = itemView.findViewById(R.id.audioPlayerLayout)
        fun bind(chatMessage: MainChat.ChatMessage) {
            if (chatMessage.type == "audio") {
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.VISIBLE
                if(chatMessage.isSent){
                    progressBar.visibility = View.GONE
                    setupAudioPlayer(audioPlayerView, progressBar,chatMessage.content)
                }
                else
                {
                    progressBar.visibility = View.VISIBLE
                }

            } else {
                messageTextView.visibility = View.VISIBLE
                audioPlayerLayout.visibility = View.GONE
                messageTextView.text = chatMessage.content
            }
            timestampTextView.text = formatTimestamp(chatMessage.time)
            // Update lastSenderId
            lastSenderId = chatMessage.sendId
        }
        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return formatter.format(date)
        }
    }

    // ViewHolder for received messages
    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val audioPlayerView: ImageView = itemView.findViewById(R.id.audioPlayerView)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.audioProgressBar)
        private val audioPlayerLayout : ConstraintLayout = itemView.findViewById(R.id.audioPlayerLayout)

        fun bind(chatMessage: MainChat.ChatMessage) {
            if (chatMessage.type == "audio") {
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.VISIBLE
                setupAudioPlayer(audioPlayerView,progressBar, chatMessage.content)
            } else {
                messageTextView.visibility = View.VISIBLE
                audioPlayerLayout.visibility = View.GONE
                messageTextView.text = chatMessage.content
            }
            timestampTextView.text = formatTimestamp(chatMessage.time)
            // Update lastSenderId
            lastSenderId = chatMessage.sendId
        }

        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return formatter.format(date)
        }
    }

    // ViewHolder for messages with avatar
    inner class MessageWithAvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)

        fun bind(chatMessage: MainChat.ChatMessage) {
            messageTextView.text = chatMessage.content
            timestampTextView.text = formatTimestamp(chatMessage.time)
            senderNameTextView.text = chatMessage.senderName

            // Load actual avatar URL here
            Glide.with(itemView.context)
                .load(chatMessage.avatarUrl) // Use the actual avatar URL
                .circleCrop()
                .placeholder(R.drawable.ic_avatar) // Placeholder image
                .into(avatarImageView)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return formatter.format(date)
        }
    }

    // ViewHolder for messages without avatar
    inner class MessageNoAvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
//
//        fun bind(chatMessage: MainChat.ChatMessage) {
//            messageTextView.text = chatMessage.content
//            timestampTextView.text = formatTimestamp(chatMessage.time)
//        }
//        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
//        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
//        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)

        fun bind(chatMessage: MainChat.ChatMessage) {
            messageTextView.text = chatMessage.content
            timestampTextView.text = formatTimestamp(chatMessage.time)

        }
        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return formatter.format(date)
        }
    }

}


