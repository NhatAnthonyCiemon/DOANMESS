package com.example.doanmess

class DataMess {
    var avatar: Int=0
    var name: String= ""
    var message: String=""
    var time: String=""
    var status : Boolean = false
    constructor(avatar: Int, name: String, message: String, time: String, status: Boolean) {
        this.avatar = avatar
        this.name = name
        this.message = message
        this.time = time
        this.status = status
    }
}