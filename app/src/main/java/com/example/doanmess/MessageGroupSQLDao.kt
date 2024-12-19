package com.example.doanmess
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.doanmess.models.MessageGroupSQL

@Dao
interface  MessageGroupSQLDao {
    @Query("SELECT * FROM group_messages")
    fun getAllGroupMessages(): MutableList<MessageGroupSQL>

    @Insert
    fun insertListGroupMessage(messages: MutableList<MessageGroupSQL>)

    @Query("DELETE FROM group_messages")
    fun deleteAllGroupMessages()
}