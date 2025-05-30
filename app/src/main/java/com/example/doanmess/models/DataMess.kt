package com.example.doanmess.models

import com.example.doanmess.MyApp
import com.example.doanmess.R
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
    var isNotify: Boolean = true
    constructor(avatar: String?, uid: String, name: String, message: String, timestamp: Long, status: Boolean, othersend: Boolean,isNotify:Boolean, isGroup: Boolean = false,isSQL:Boolean = false) {
        this.uid = uid
        this.avatar = avatar
        this.name = name
        this.status = status
        this.last_name = name.split(" ").last()
        this.othersend = othersend
        val sender = MyApp.instance.getString(R.string.You)
        if(!isSQL) this.message = if(!othersend) "$sender: $message" else last_name + ": $message"
        else this.message = message
        this.timestamp = timestamp
        this.time = convertTimestampToString(timestamp)
        this.isNotify = isNotify
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

    constructor(avatar: String?, uid: String, name: String, message: String, timestamp: Long, status: Boolean, whosend: String, groupname: String, isNotify:Boolean, isGroup: Boolean = true, isSQL: Boolean =false)
            : super(avatar, uid, name, message, timestamp, status, true, isNotify,isGroup, isSQL) {
        this.groupname = groupname
        if(!isSQL) this.message = "$whosend: $message"
        else this.message = message
        this.isGroup = true
    }
}