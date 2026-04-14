package com.coddit.app.data.local.db

import androidx.room.*
import com.coddit.app.data.local.db.dao.PostDao
import com.coddit.app.data.local.db.dao.ReplyDao
import com.coddit.app.data.local.db.dao.UserDao
import com.coddit.app.data.local.db.entity.PostEntity
import com.coddit.app.data.local.db.entity.ReplyEntity
import com.coddit.app.data.local.db.entity.UserEntity

@Database(
    entities = [PostEntity::class, ReplyEntity::class, UserEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(CodditTypeConverters::class)
abstract class CodditDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun replyDao(): ReplyDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "coddit_db"
    }
}

class CodditTypeConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split(",")
}
