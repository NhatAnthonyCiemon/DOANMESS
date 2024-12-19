package com.example.doanmess.models

class ChatMessage(
    val content: String,
    val time: Long,
    val sendId: String,
    val avatarUrl: String?, // URL for sender's avatar
    val senderName: String?, // Sender's name
    val showSenderInfo: Boolean // Flag to indicate whether to show avatar and name
)