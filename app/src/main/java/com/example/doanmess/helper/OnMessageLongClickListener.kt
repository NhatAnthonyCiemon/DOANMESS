package com.example.doanmess.helper

import com.example.doanmess.activities.MainChat

interface OnMessageLongClickListener {

    fun onMessageLongClick(position: Int, message: MainChat.ChatMessage)
}