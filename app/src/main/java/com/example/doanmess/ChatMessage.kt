package com.example.createuiproject

data class ChatMessage(
    val message: String,
    val isSent: Boolean // true for sent messages, false for received messages
)
