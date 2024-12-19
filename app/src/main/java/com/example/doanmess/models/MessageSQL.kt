package com.example.doanmess.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Entity(tableName = "messages")
open class MessageSQL(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "uid") var uid: String = "",
    @ColumnInfo(name = "avatar") var avatar: String? = "",
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "message") var message: String = "",
    @ColumnInfo(name = "time") var time: String = "",
    @ColumnInfo(name = "status") var status: Boolean = false,
    @ColumnInfo(name = "othersend") var othersend: Boolean = false,
    @ColumnInfo(name = "last_name") var lastName: String = "",
    @ColumnInfo(name = "timestamp") var timestamp: Long = 0,
    @ColumnInfo(name = "is_group") var isGroup: Boolean = false,
    @ColumnInfo(name = "is_notify") var isNotify: Boolean = true
) {
    constructor(
        avatar: String?,
        uid: String,
        name: String,
        message: String,
        timestamp: Long,
        status: Boolean,
        othersend: Boolean,
        isNotify: Boolean,
        isGroup: Boolean = false
    ) : this() {
        this.uid = uid
        this.avatar = avatar
        this.name = name
        this.status = status
        this.lastName = name.split(" ").last()
        this.othersend = othersend
        this.message = message
        this.timestamp = timestamp
        this.time = convertTimestampToString(timestamp)
        this.isNotify = isNotify
        this.isGroup = isGroup
    }

    fun convertTimestampToString(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val sdf = SimpleDateFormat("E HH:mm", Locale.getDefault())
        sdf.timeZone = timeZone
        val date = Date(timestamp)
        return sdf.format(date)
    }
}

@Entity(tableName = "group_messages")
class MessageGroupSQL(
    @ColumnInfo(name = "group_name") var groupName: String = ""
) : MessageSQL() {
    constructor(
        avatar: String?,
        uid: String,
        name: String,
        message: String,
        timestamp: Long,
        status: Boolean,
        whosend: String,
        groupName: String,
        isNotify: Boolean,
        isGroup: Boolean = true
    ) : this() {
        this.uid = uid
        this.avatar = avatar
        this.name = name
        this.status = status
        this.groupName = groupName
        this.othersend = true
        this.lastName = name.split(" ").last()
        this.message = message
        this.timestamp = timestamp
        this.time = convertTimestampToString(timestamp)
        this.isNotify = isNotify
        this.isGroup = isGroup
    }
}