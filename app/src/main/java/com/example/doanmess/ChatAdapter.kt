package com.example.createuiproject
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.doanmess.MainChat
import com.example.doanmess.OnMessageLongClickListener
import com.example.doanmess.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import android.media.MediaMetadataRetriever

class ChatAdapter(private val chatMessages: MutableList<MainChat.ChatMessage>, val isGroup: Boolean, private val listener: OnMessageLongClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var auth = Firebase.auth
    private var currentUser = auth.currentUser

    // Listener interface
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var OnClicklistener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.OnClicklistener = listener
    }


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

        // Set click listener for the item view
        holder.itemView.setOnClickListener {
            OnClicklistener?.onItemClick(position)
        }

        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is MessageWithAvatarViewHolder -> holder.bind(message)
            is MessageNoAvatarViewHolder -> holder.bind(message)
        }
    }

    fun releaseResources() {
        // Release the ExoPlayer
        if(::player.isInitialized) player.release()
        for((_, player) in audioPlayerLists){
            player.release()
        }
    }
    override fun getItemCount(): Int {
        return chatMessages.size
    }
    private lateinit var player : ExoPlayer

    fun setUpVideoPlayer(chatId:String, context: Context, playerView: PlayerView, content: String){
        if(!audioPlayerLists.containsKey(chatId)){
            val tmp = ExoPlayer.Builder(context).build()
            audioPlayerLists[chatId] = tmp
            // Thiết lập video URL
            val mediaItem = MediaItem.fromUri(content)
            tmp!!.setMediaItem(mediaItem)
        }
        val tmp = audioPlayerLists[chatId]
        tmp!!.prepare()
        // Khởi tạo ExoPlayer từ Media3
        playerView.player = tmp
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        // Giải phóng tài nguyên của videoMessageView nếu cần
        if (holder is ReceivedMessageViewHolder) {
            releaseVideoPlayer(holder.videoMessageView)
        } else if (holder is MessageWithAvatarViewHolder) {
            releaseVideoPlayer(holder.videoMessageView)
        } else if (holder is MessageNoAvatarViewHolder) {
            releaseVideoPlayer(holder.videoMessageView)
        }
    }

    // Hàm tiện ích để giải phóng PlayerView
    private fun releaseVideoPlayer(playerView: PlayerView?) {
        val player = playerView?.player
        if (player != null) {
            player.stop() // Dừng phát
            player.release() // Giải phóng tài nguyên
        }
        playerView?.player = null // Xóa liên kết

    }


    fun releaseAllPlayers() {
        for ((_, player) in audioPlayerLists) {
            player.release() // Giải phóng tài nguyên
        }
        audioPlayerLists.clear() // Xóa tất cả khỏi danh sách
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        releaseAllPlayers() // Giải phóng toàn bộ player
    }

    // ViewHolder for sent messages
    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMessageLongClick(position, chatMessages[position])
                }
                true // Trả về true để xử lý sự kiện long click
            }
        }
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val imageMessageView: ImageView = itemView.findViewById(R.id.imageMessageView)
        val videoMessageView: PlayerView = itemView.findViewById(R.id.videoMessageView)
        private val cardVideo: CardView = itemView.findViewById(R.id.cardVideo)
        private val loadingBar : ProgressBar = itemView.findViewById(R.id.LoadingBar)
        private val audioPlayerLayout : CardView = itemView.findViewById(R.id.audioPlayerLayout)
        private val audioPlayerView: PlayerView = itemView.findViewById(R.id.audioPlayerView)
        private val audioPlayBtn : ImageButton = itemView.findViewById(R.id.audioPlayBtn)
        fun bind(chatMessage: MainChat.ChatMessage) {
            if (chatMessage.type == "video") {
                messageTextView.visibility = View.GONE
                imageMessageView.visibility = View.VISIBLE // Hiển thị thumbnail trước
                videoMessageView.visibility = View.GONE // Ẩn player khi chưa phát
                audioPlayerLayout.visibility = View.GONE
                cardVideo.visibility = View.GONE

                // Show a placeholder while loading the thumbnail
                Glide.with(itemView.context)
                    .load(chatMessage.content) // Use video URL as a placeholder for the thumbnail
                    .placeholder(R.drawable.video_placeholder) // Placeholder image
                    .error(R.drawable.video_placeholder) // Fallback image in case of error
                    .into(imageMessageView)

                // Retrieve video thumbnail in background thread
                CoroutineScope(Dispatchers.IO).launch {
                    val retriever = MediaMetadataRetriever()
                    var bitmap: Bitmap? = null
                    try {
                        retriever.setDataSource(chatMessage.content, HashMap()) // Use video URL
                        bitmap = retriever.frameAtTime // Capture the first frame as a thumbnail
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        retriever.release() // Release resources
                    }

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            Glide.with(itemView.context)
                                .load(bitmap) // Use extracted bitmap
                                .placeholder(R.drawable.video_placeholder) // Placeholder image
                                .error(R.drawable.video_placeholder) // Fallback image in case of error
                                .into(imageMessageView)
                        }
                    }
                }

                // Set click listener to start video playback
                imageMessageView.setOnClickListener {
                    imageMessageView.visibility = View.GONE // Hide thumbnail when video starts playing
                    videoMessageView.visibility = View.VISIBLE // Show the video player
                    cardVideo.visibility = View.VISIBLE
                    setUpVideoPlayer(chatMessage.chatId, itemView.context, videoMessageView, chatMessage.content)
                }

                // Long click listener on the video card
                videoMessageView.setOnLongClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMessageLongClick(position, chatMessages[position])
                    }
                    true // Return true to handle long click event
                }
            }

            else if (chatMessage.type == "image") {
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.GONE
                imageMessageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(chatMessage.content) // Đường dẫn ảnh
                    .transform(CenterCrop(), RoundedCorners(16)) // Bo góc 16dp
                    .placeholder(R.drawable.image_placeholder) // Ảnh chờ
                    .error(R.drawable.image_error) // Ảnh lỗi
                    .into(imageMessageView)
            }
            else if (chatMessage.type == "audio") {
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.VISIBLE
                if (chatMessage.isSent) {
                    loadingBar.visibility = View.GONE
                //    audioPlayerView.visibility= View.VISIBLE
                    setupAudioPlayer(audioPlayBtn,chatMessage.chatId ,itemView.context, audioPlayerView, chatMessage.content)
                } else {
                    audioPlayerView.visibility= View.GONE
                    loadingBar.visibility = View.VISIBLE
                }
            } else {
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                messageTextView.visibility = View.VISIBLE
                audioPlayerLayout.visibility = View.GONE
                messageTextView.text = chatMessage.content
            }
            timestampTextView.text = formatTimestamp(chatMessage.time)
            lastSenderId = chatMessage.sendId
        }
        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return formatter.format(date)
        }
    }

    val audioPlayerLists : MutableMap<String,ExoPlayer> = mutableMapOf()
    private fun setupAudioPlayer(audioPlayBtn : ImageButton,chatId:String, context: Context, audioPlayerView: PlayerView, content: String) {
        audioPlayerView.player = null
        audioPlayerView.visibility = View.GONE
        audioPlayBtn.setOnClickListener{
            audioPlayerView.visibility = View.VISIBLE
            if(!audioPlayerLists.containsKey(chatId)){
                val tmp = ExoPlayer.Builder(context).build()
                val mediaItem = MediaItem.fromUri(content)
                tmp!!.setMediaItem(mediaItem)
                audioPlayerLists[chatId] = tmp
                //      tmp.pause()
            }
            val tmp = audioPlayerLists[chatId]
            audioPlayerView.player = tmp
            tmp!!.prepare()
            audioPlayerView.player!!.playWhenReady = true

        }
    }




    // ViewHolder for received messages
    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMessageLongClick(position, chatMessages[position])
                }
                true // Trả về true để xử lý sự kiện long click
            }
        }
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val imageMessageView: ImageView = itemView.findViewById(R.id.imageMessageView)
        val videoMessageView: PlayerView = itemView.findViewById(R.id.videoMessageView)
        private val cardVideo: CardView = itemView.findViewById(R.id.cardVideo)
        private val audioPlayerLayout : CardView = itemView.findViewById(R.id.audioPlayerLayout)
        private val audioPlayerView: PlayerView = itemView.findViewById(R.id.audioPlayerView)
        private val audioPlayBtn : ImageButton = itemView.findViewById(R.id.audioPlayBtn)
        fun bind(chatMessage: MainChat.ChatMessage) {
            if (chatMessage.type == "video") {
                messageTextView.visibility = View.GONE
                imageMessageView.visibility = View.VISIBLE // Hiển thị thumbnail trước
                videoMessageView.visibility = View.GONE // Ẩn player khi chưa phát
                audioPlayerLayout.visibility = View.GONE
                cardVideo.visibility = View.GONE

                // Show a placeholder while loading the thumbnail
                Glide.with(itemView.context)
                    .load(chatMessage.content) // Use video URL as a placeholder for the thumbnail
                    .placeholder(R.drawable.video_placeholder) // Placeholder image
                    .error(R.drawable.video_placeholder) // Fallback image in case of error
                    .into(imageMessageView)

                // Retrieve video thumbnail in background thread
                CoroutineScope(Dispatchers.IO).launch {
                    val retriever = MediaMetadataRetriever()
                    var bitmap: Bitmap? = null
                    try {
                        retriever.setDataSource(chatMessage.content, HashMap()) // Use video URL
                        bitmap = retriever.frameAtTime // Capture the first frame as a thumbnail
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        retriever.release() // Release resources
                    }

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            Glide.with(itemView.context)
                                .load(bitmap) // Use extracted bitmap
                                .placeholder(R.drawable.video_placeholder) // Placeholder image
                                .error(R.drawable.video_placeholder) // Fallback image in case of error
                                .into(imageMessageView)
                        }
                    }
                }

                // Set click listener to start video playback
                imageMessageView.setOnClickListener {
                    imageMessageView.visibility = View.GONE // Hide thumbnail when video starts playing
                    videoMessageView.visibility = View.VISIBLE // Show the video player
                    cardVideo.visibility = View.VISIBLE
                    setUpVideoPlayer(chatMessage.chatId, itemView.context, videoMessageView, chatMessage.content)
                }

                // Long click listener on the video card
                videoMessageView.setOnLongClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMessageLongClick(position, chatMessages[position])
                    }
                    true // Return true to handle long click event
                }
            }
            else if (chatMessage.type == "image") {
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.GONE
                imageMessageView.visibility = View.VISIBLE
                videoMessageView.visibility = View.VISIBLE
                cardVideo.visibility = View.GONE

                Glide.with(itemView.context)
                    .load(chatMessage.content) // Đường dẫn ảnh
                    .transform(CenterCrop(), RoundedCorners(16)) // Bo góc 16dp
                    .placeholder(R.drawable.image_placeholder) // Ảnh chờ
                    .error(R.drawable.image_error) // Ảnh lỗi
                    .into(imageMessageView)
            }
            else if (chatMessage.type == "audio") {
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.VISIBLE
                audioPlayerView.visibility= View.VISIBLE
                setupAudioPlayer(audioPlayBtn,chatMessage.chatId ,itemView.context, audioPlayerView, chatMessage.content)
            } else {
                cardVideo.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                messageTextView.visibility = View.VISIBLE
                audioPlayerLayout.visibility = View.GONE
                messageTextView.text = chatMessage.content
                videoMessageView.visibility = View.GONE
            }
            timestampTextView.text = formatTimestamp(chatMessage.time)
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
        init {
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMessageLongClick(position, chatMessages[position])
                }
                true // Trả về true để xử lý sự kiện long click
            }
        }
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
        private val imageMessageView: ImageView = itemView.findViewById(R.id.imageMessageView)
        val videoMessageView: PlayerView = itemView.findViewById(R.id.videoMessageView)
        private val cardVideo: CardView = itemView.findViewById(R.id.cardVideo)
        private val audioPlayerView: PlayerView = itemView.findViewById(R.id.audioPlayerView)
        private val audioPlayerLayout : CardView = itemView.findViewById(R.id.audioPlayerLayout)
        private val audioPlayBtn : ImageButton = itemView.findViewById(R.id.audioPlayBtn)
        fun bind(chatMessage: MainChat.ChatMessage) {
            if (chatMessage.type == "video") {
                messageTextView.visibility = View.GONE
                imageMessageView.visibility = View.VISIBLE // Hiển thị thumbnail trước
                videoMessageView.visibility = View.GONE // Ẩn player khi chưa phát
                audioPlayerLayout.visibility = View.GONE
                cardVideo.visibility = View.GONE

                // Show a placeholder while loading the thumbnail
                Glide.with(itemView.context)
                    .load(chatMessage.content) // Use video URL as a placeholder for the thumbnail
                    .placeholder(R.drawable.video_placeholder) // Placeholder image
                    .error(R.drawable.video_placeholder) // Fallback image in case of error
                    .into(imageMessageView)

                // Retrieve video thumbnail in background thread
                CoroutineScope(Dispatchers.IO).launch {
                    val retriever = MediaMetadataRetriever()
                    var bitmap: Bitmap? = null
                    try {
                        retriever.setDataSource(chatMessage.content, HashMap()) // Use video URL
                        bitmap = retriever.frameAtTime // Capture the first frame as a thumbnail
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        retriever.release() // Release resources
                    }

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            Glide.with(itemView.context)
                                .load(bitmap) // Use extracted bitmap
                                .placeholder(R.drawable.video_placeholder) // Placeholder image
                                .error(R.drawable.video_placeholder) // Fallback image in case of error
                                .into(imageMessageView)
                        }
                    }
                }

                // Set click listener to start video playback
                imageMessageView.setOnClickListener {
                    imageMessageView.visibility = View.GONE // Hide thumbnail when video starts playing
                    videoMessageView.visibility = View.VISIBLE // Show the video player
                    cardVideo.visibility = View.VISIBLE
                    setUpVideoPlayer(chatMessage.chatId, itemView.context, videoMessageView, chatMessage.content)
                }

                // Long click listener on the video card
                videoMessageView.setOnLongClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMessageLongClick(position, chatMessages[position])
                    }
                    true // Return true to handle long click event
                }
            }
            else if (chatMessage.type == "image") {
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.GONE
                imageMessageView.visibility = View.VISIBLE
                videoMessageView.visibility = View.VISIBLE
                cardVideo.visibility = View.GONE
                Glide.with(itemView.context)
                    .load(chatMessage.content) // Đường dẫn ảnh
                    .transform(CenterCrop(), RoundedCorners(16)) // Bo góc 16dp
                    .placeholder(R.drawable.image_placeholder) // Ảnh chờ
                    .error(R.drawable.image_error) // Ảnh lỗi
                    .into(imageMessageView)
            }
            else if (chatMessage.type == "audio") {
                messageTextView.visibility = View.GONE
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                audioPlayerLayout.visibility = View.VISIBLE
                audioPlayerView.visibility= View.VISIBLE
                setupAudioPlayer(audioPlayBtn,chatMessage.chatId ,itemView.context, audioPlayerView, chatMessage.content)
            } else {
                cardVideo.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                messageTextView.visibility = View.VISIBLE
                audioPlayerLayout.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                cardVideo.visibility = View.GONE
                messageTextView.text = chatMessage.content
            }
            timestampTextView.text = formatTimestamp(chatMessage.time)
            // Update lastSenderId
            lastSenderId = chatMessage.sendId

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
        init {
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMessageLongClick(position, chatMessages[position])
                }
                true // Trả về true để xử lý sự kiện long click
            }
        }
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val imageMessageView: ImageView = itemView.findViewById(R.id.imageMessageView)
        private val audioPlayerLayout : CardView = itemView.findViewById(R.id.audioPlayerLayout)
        private val audioPlayerView: PlayerView = itemView.findViewById(R.id.audioPlayerView)
        val videoMessageView: PlayerView = itemView.findViewById(R.id.videoMessageView)
        private val cardVideo: CardView = itemView.findViewById(R.id.cardVideo)
        private val audioPlayBtn : ImageButton = itemView.findViewById(R.id.audioPlayBtn)
//
//        fun bind(chatMessage: MainChat.ChatMessage) {
//            messageTextView.text = chatMessage.content
//            timestampTextView.text = formatTimestamp(chatMessage.time)
//        }
//        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
//        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
//        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)

        fun bind(chatMessage: MainChat.ChatMessage) {
            // Trong phương thức bind hoặc xử lý
            if (chatMessage.type == "video") {
                messageTextView.visibility = View.GONE
                imageMessageView.visibility = View.VISIBLE // Hiển thị thumbnail trước
                videoMessageView.visibility = View.GONE // Ẩn player khi chưa phát
                audioPlayerLayout.visibility = View.GONE
                cardVideo.visibility = View.GONE

                // Show a placeholder while loading the thumbnail
                Glide.with(itemView.context)
                    .load(chatMessage.content) // Use video URL as a placeholder for the thumbnail
                    .placeholder(R.drawable.video_placeholder) // Placeholder image
                    .error(R.drawable.video_placeholder) // Fallback image in case of error
                    .into(imageMessageView)

                // Retrieve video thumbnail in background thread
                CoroutineScope(Dispatchers.IO).launch {
                    val retriever = MediaMetadataRetriever()
                    var bitmap: Bitmap? = null
                    try {
                        retriever.setDataSource(chatMessage.content, HashMap()) // Use video URL
                        bitmap = retriever.frameAtTime // Capture the first frame as a thumbnail
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        retriever.release() // Release resources
                    }

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                        val context = itemView.context
                        val activity = context as? Activity

                        // Kiểm tra nếu context không phải là Activity hoặc Activity đã bị hủy
                        if (activity == null || activity.isDestroyed || activity.isFinishing) {
                            return@withContext
                        }
                        try {
                            if (bitmap != null) {
                                Glide.with(itemView.context)
                                    .load(bitmap) // Use extracted bitmap
                                    .placeholder(R.drawable.video_placeholder) // Placeholder image
                                    .error(R.drawable.video_placeholder) // Fallback image in case of error
                                    .into(imageMessageView)
                            }
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // Set click listener to start video playback
                imageMessageView.setOnClickListener {
                    imageMessageView.visibility = View.GONE // Hide thumbnail when video starts playing
                    videoMessageView.visibility = View.VISIBLE // Show the video player
                    cardVideo.visibility = View.VISIBLE
                    setUpVideoPlayer(chatMessage.chatId, itemView.context, videoMessageView, chatMessage.content)
                }

                // Long click listener on the video card
                videoMessageView.setOnLongClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMessageLongClick(position, chatMessages[position])
                    }
                    true // Return true to handle long click event
                }
            }
 else if (chatMessage.type == "image") {
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.GONE
                imageMessageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(chatMessage.content) // Đường dẫn ảnh
                    .transform(CenterCrop(), RoundedCorners(16)) // Bo góc 16dp
                    .placeholder(R.drawable.image_placeholder) // Ảnh chờ
                    .error(R.drawable.image_error) // Ảnh lỗi
                    .into(imageMessageView)
            }
            else if (chatMessage.type == "audio") {
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.VISIBLE
                audioPlayerView.visibility= View.VISIBLE
                setupAudioPlayer(audioPlayBtn,chatMessage.chatId ,itemView.context, audioPlayerView, chatMessage.content)
            } else {
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                messageTextView.visibility = View.VISIBLE
                audioPlayerLayout.visibility = View.GONE
                messageTextView.text = chatMessage.content
            }
            timestampTextView.text = formatTimestamp(chatMessage.time)

        }
        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return formatter.format(date)
        }

    }



}


