package com.example.doanmess

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MessageSQL::class, MessageGroupSQL::class], version = 1)
abstract class MessageSQLDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageSQLDao
    abstract fun messageGroupDao(): MessageGroupSQLDao

    companion object {
        private const val DB_NAME = "messages_db"

        @Volatile
        private var instance: MessageSQLDatabase? = null

        fun getInstance(context: Context): MessageSQLDatabase{
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, MessageSQLDatabase::class.java, DB_NAME)
                .allowMainThreadQueries()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                })
                .build()
    }
}
