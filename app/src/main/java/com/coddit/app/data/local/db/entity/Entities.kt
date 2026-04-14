package com.coddit.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val postId: String,
    val authorUid: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val title: String,
    val body: String,
    val codeSnippet: String?,
    val imageUrlsJson: String,      // JSON array string
    val tagsJson: String,          // JSON array string
    val upvotes: Int,
    val viewCount: Int,
    val replyCount: Int,
    val solved: Boolean,
    val acceptedReplyId: String?,
    val createdAt: Long,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "replies",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["postId"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("postId")]
)
data class ReplyEntity(
    @PrimaryKey val replyId: String,
    val postId: String,
    val authorUid: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val body: String,
    val imageUrlsJson: String,      // JSON array string
    val linksJson: String,         // JSON array string
    val accepted: Boolean,
    val upvotes: Int,
    val createdAt: Long,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val bytes: Int,
    val postCount: Int,
    val solvedCount: Int,
    val followerCount: Int = 0,
    val linkedAccountsJson: String, // JSON array string
    val skillsJson: String = "[]",
    val cachedAt: Long = System.currentTimeMillis()
)
