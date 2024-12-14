package com.example.createuiproject
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.media3.common.MediaItem
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
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.media.MediaMetadataRetriever
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ChatAdapter(private val chatMessages: MutableList<MainChat.ChatMessage>, val isGroup: Boolean, private val listener: OnMessageLongClickListener, private val avatarUrlMapping: Map<String, String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var auth = Firebase.auth
    private var currentUser = auth.currentUser
    private var onItemClickListener: OnItemClickListener? = null
    private var lastSenderId: String =""
    private lateinit var player : ExoPlayer
    val audioPlayerLists : MutableMap<String,ExoPlayer> = mutableMapOf()

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
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
        val timeStampTextView: TextView = itemView.findViewById(R.id.timestampTextView) // Your TextView for the sent message
    }

//    override fun getItemViewType(position: Int): Int {
//
//        val currentMessage = chatMessages[position]
//        if (position == 0) {
//            lastSenderId = ""
//        }
//        // Check if the current message is sent by the current user
//        if (currentMessage.sendId == currentUser?.uid) {
//            lastSenderId = currentMessage.sendId
//            return VIEW_TYPE_SENT
//        } else if (isGroup) {
//            // Check if this message's sender ID is the same as the last message's sender ID
//            return if (currentMessage.sendId == lastSenderId) {
//                VIEW_TYPE_NO_AVATAR // Same sender, no avatar
//            } else {
//                // Different sender, update lastSenderId
//                lastSenderId = currentMessage.sendId
//                VIEW_TYPE_WITH_AVATAR // New sender, show avatar and name
//            }
//        } else {
//            return VIEW_TYPE_RECEIVED
//        }
//    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = chatMessages[position]

        // Default to first message with avatar if it's the first position
        if (position == 0) {
            return if (isGroup && currentMessage.sendId != currentUser?.uid) {
                VIEW_TYPE_WITH_AVATAR
            } else if (currentMessage.sendId == currentUser?.uid) {
                VIEW_TYPE_SENT
            } else {
                VIEW_TYPE_RECEIVED
            }
        }

        // Get the previous message
        val previousMessage = chatMessages[position - 1]

        // Check if current message is sent by current user
        if (currentMessage.sendId == currentUser?.uid) {
            return VIEW_TYPE_SENT
        }

        // If in a group, decide based on the sender ID of the previous message
        return if (isGroup) {
            if (currentMessage.sendId == previousMessage.sendId) {
                VIEW_TYPE_NO_AVATAR // Same sender as the previous message
            } else {
                VIEW_TYPE_WITH_AVATAR // Different sender
            }
        } else {
            VIEW_TYPE_RECEIVED
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
            onItemClickListener?.onItemClick(position)
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

    // Hàm để giải phóng PlayerView
    private fun releaseVideoPlayer(playerView: PlayerView?) {
        val player = playerView?.player
        if (player != null) {
            player.stop() // Dừng phát
            player.release() // Giải phóng tài nguyên
        }
        playerView?.player = null // Xóa liên kết
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

    private fun releaseAllPlayers() {
        for ((_, player) in audioPlayerLists) {
            player.release() // Giải phóng tài nguyên
        }
        audioPlayerLists.clear() // Xóa tất cả khỏi danh sách
    }

    private fun showZoomedImageDialog(context: Context, imageUrl: String) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_zoom_image)
        val zoomedImageView = dialog.findViewById<ImageView>(R.id.zoomed_image)
        Glide.with(context)
            .load(imageUrl)
            .into(zoomedImageView)
        dialog.show()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        releaseAllPlayers() // Giải phóng toàn bộ player
    }

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
        private val videoMessageView: PlayerView = itemView.findViewById(R.id.videoMessageView)
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
                            val context = itemView.context
                            val activity = context as? Activity

                            // Kiểm tra nếu context không phải là Activity hoặc Activity đã bị hủy
                            if (activity == null || activity.isDestroyed || activity.isFinishing) {
                                return@withContext
                            }
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
                imageMessageView.setOnClickListener {
                    showZoomedImageDialog(itemView.context, chatMessage.content)
                }
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
            }

            else {
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
                            val context = itemView.context
                            val activity = context as? Activity

                            // Kiểm tra nếu context không phải là Activity hoặc Activity đã bị hủy
                            if (activity == null || activity.isDestroyed || activity.isFinishing) {
                                return@withContext
                            }
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

                imageMessageView.setOnClickListener {
                    showZoomedImageDialog(itemView.context, chatMessage.content)
                }
            }

            else if (chatMessage.type == "audio") {
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.VISIBLE
                audioPlayerView.visibility= View.VISIBLE
                setupAudioPlayer(audioPlayBtn,chatMessage.chatId ,itemView.context, audioPlayerView, chatMessage.content)
            }

            else {
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
        val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
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
                            val context = itemView.context
                            val activity = context as? Activity

                            // Kiểm tra nếu context không phải là Activity hoặc Activity đã bị hủy
                            if (activity == null || activity.isDestroyed || activity.isFinishing) {
                                return@withContext
                            }
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
                imageMessageView.setOnClickListener {
                    showZoomedImageDialog(itemView.context, chatMessage.content)
                }
                imageMessageView.setOnClickListener {
                    showZoomedImageDialog(itemView.context, chatMessage.content)
                }
            }

            else if (chatMessage.type == "audio") {
                messageTextView.visibility = View.GONE
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                audioPlayerLayout.visibility = View.VISIBLE
                audioPlayerView.visibility= View.VISIBLE
                setupAudioPlayer(audioPlayBtn,chatMessage.chatId ,itemView.context, audioPlayerView, chatMessage.content)
            }

            else {
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
            val senderUID = chatMessage.sendId

            val avatarUrl = avatarUrlMapping[senderUID]
            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_error)
                    .into(avatarImageView)
            } else {
                avatarImageView.setImageResource(R.drawable.image_placeholder)
            }

//            Glide.with(itemView.context).clear(avatarImageView)
//            Glide.with(itemView.context)
//                .load(avatarUrl)
//                .circleCrop()
//                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache cả ảnh gốc và ảnh resize
//                .placeholder(R.drawable.image_placeholder)
//                .error(R.drawable.image_error)
//                .into(avatarImageView)


//            Glide.with(itemView.context)
//                .load(avatarUrl) // Use the actual avatar URL
//                .circleCrop()
//                .placeholder(R.drawable.ic_avatar) // Placeholder image
//                .into(avatarImageView)
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
                imageMessageView.setOnClickListener {
                    showZoomedImageDialog(itemView.context, chatMessage.content)
                }
            }

            else if (chatMessage.type == "audio") {
                cardVideo.visibility = View.GONE
                videoMessageView.visibility = View.GONE
                imageMessageView.visibility = View.GONE
                messageTextView.visibility = View.GONE
                audioPlayerLayout.visibility = View.VISIBLE
                audioPlayerView.visibility= View.VISIBLE
                setupAudioPlayer(audioPlayBtn,chatMessage.chatId ,itemView.context, audioPlayerView, chatMessage.content)
            }

            else {
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


