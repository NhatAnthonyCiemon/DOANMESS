package com.example.doanmess

class Contact {
    var id  = ""
    var avatar: String=""
    var name: String= ""
    var isGroup: Boolean = false
    var online : Boolean = false
    constructor(id:String, avatar: String, name: String, online: Boolean, isGroup: Boolean) {
        this.avatar = avatar
        this.name = name
        this.online = online
        this.id = id
        this.isGroup = isGroup
    }

}