package com.example.doanmess

class Contact {
    var id  = ""
    var avatar: String=""
    var name: String= ""
    var online : Boolean = false
    constructor(id:String, avatar: String, name: String, online: Boolean) {
        this.avatar = avatar
        this.name = name
        this.online = online
        this.id = id
    }

}