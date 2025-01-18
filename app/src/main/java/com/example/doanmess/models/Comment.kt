package com.example.doanmess.models

data class Comment (
    val uid: String,
    val id: String,
    val profilePic: String,
    val username: String,
    val time: Long,
    var likes: ArrayList<String>,
    var liked: Boolean,
    val content: String,
){
}