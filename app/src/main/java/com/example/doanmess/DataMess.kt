package com.example.doanmess

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

open class DataMess {
    var uid: String= ""
    var avatar: String?= ""
    var name: String= ""
    var message: String=""
    var time: String=""
    var status : Boolean = false
    var othersend: Boolean = false
    var last_name: String = ""
    var timestamp: Long = 0
    var isGroup: Boolean = false
    constructor(avatar: String?, uid: String, name: String, message: String, timestamp: Long, status: Boolean, othersend: Boolean, isGroup: Boolean = false) {
        this.uid = uid
        this.avatar = avatar
        this.name = name
        this.status = status
        this.last_name = name.split(" ").last()
        this.othersend = othersend
        this.message = if(!othersend) "Bạn: $message" else last_name + ": $message"
        this.timestamp = timestamp
        this.time = convertTimestampToString(timestamp)
    }
    fun convertTimestampToString(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val sdf = SimpleDateFormat("E HH:mm", Locale.getDefault())
        sdf.timeZone = timeZone
        val date = Date(timestamp)
        return sdf.format(date)
    }
}

class DataMessGroup : DataMess {
    var groupname: String = ""

    constructor(avatar: String?, uid: String, name: String, message: String, timestamp: Long, status: Boolean, whosend: String, groupname: String, isGroup: Boolean = true)
            : super(avatar, uid, name, message, timestamp, status, true) {
        this.groupname = groupname
        this.message = "$whosend: $message"
    }
}