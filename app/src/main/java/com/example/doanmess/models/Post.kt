package com.example.doanmess.models

data class Post (
    val uid: String,
    val id: String,
    val profilePic: String,
    val username: String,
    val time: Long,
    val title: String,
    val mediaFile: String,
    val type : String,
    var likes: Int,
    var liked: Boolean,
    var comments: Int,
){
}