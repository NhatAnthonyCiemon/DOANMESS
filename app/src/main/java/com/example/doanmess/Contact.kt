package com.example.doanmess

class Contact {
    var id  = ""
    var avatar: Int=0
    var name: String= ""
    var online : Boolean = false
    constructor(id:String, avatar: Int, name: String, online: Boolean) {
        this.avatar = avatar
        this.name = name
        this.online = online
        this.id = id
    }
}