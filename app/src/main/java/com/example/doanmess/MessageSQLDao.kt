package com.example.doanmess

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.doanmess.models.MessageSQL


@Dao
interface MessageSQLDao {
    @Query("SELECT * FROM messages")
    fun getAllMessages(): MutableList<MessageSQL>


    @Insert
    fun insertListMessage(messages: MutableList<MessageSQL>)

    @Query("DELETE FROM messages")
    fun deleteAllMessages()


}