package com.coddit.app.di

import android.content.Context
import androidx.room.Room
import com.coddit.app.data.local.db.CodditDatabase
import com.coddit.app.data.local.db.dao.PostDao
import com.coddit.app.data.local.db.dao.ReplyDao
import com.coddit.app.data.local.db.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CodditDatabase {
        return Room.databaseBuilder(
            context,
            CodditDatabase::class.java,
            CodditDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun providePostDao(db: CodditDatabase): PostDao = db.postDao()

    @Provides
    fun provideReplyDao(db: CodditDatabase): ReplyDao = db.replyDao()

    @Provides
    fun provideUserDao(db: CodditDatabase): UserDao = db.userDao()
}
