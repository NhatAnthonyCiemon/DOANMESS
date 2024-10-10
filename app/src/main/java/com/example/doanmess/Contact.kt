package com.example.doanmess

class Contact {
    var avatar: Int=0
    var name: String= ""
    var online : Boolean = false
    constructor(avatar: Int, name: String, online: Boolean) {
        this.avatar = avatar
        this.name = name
        this.online = online
    }
}