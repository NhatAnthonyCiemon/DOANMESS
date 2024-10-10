package com.example.doanmess

class DataMess {
    var avatar: Int=0
    var name: String= ""
    var message: String=""
    var time: String=""
    var status : Boolean = false
    var othersend: Boolean = false
    var last_name: String = ""
    constructor(avatar: Int, name: String, message: String, time: String, status: Boolean, othersend: Boolean) {
        this.avatar = avatar
        this.name = name
        this.time = time
        this.status = status
        this.last_name = name.split(" ").last()
        this.othersend = othersend
        this.message = if(!othersend) "Bạn: $message" else last_name + ": $message"
    }
}