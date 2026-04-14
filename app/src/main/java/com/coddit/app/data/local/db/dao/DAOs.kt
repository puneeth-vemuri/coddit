package com.coddit.app.data.local.db.dao

import androidx.room.*
import com.coddit.app.data.local.db.entity.PostEntity
import com.coddit.app.data.local.db.entity.ReplyEntity
import com.coddit.app.data.local.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun observeAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE postId = :postId")
    suspend fun getPostById(postId: String): PostEntity?

    @Query("SELECT * FROM posts WHERE tagsJson LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun observePostsByTag(tag: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorUid = :authorUid ORDER BY createdAt DESC")
    fun observePostsByAuthor(authorUid: String): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("UPDATE posts SET title = :title, body = :body, tagsJson = :tagsJson, imageUrlsJson = :imageUrlsJson WHERE postId = :postId")
    suspend fun updatePostContent(postId: String, title: String, body: String, tagsJson: String, imageUrlsJson: String)

    @Query("DELETE FROM posts WHERE postId = :postId")
    suspend fun deletePostById(postId: String)

    @Query("UPDATE posts SET replyCount = MAX(0, replyCount + :delta) WHERE postId = :postId")
    suspend fun adjustReplyCount(postId: String, delta: Int)

    @Query("UPDATE posts SET viewCount = MAX(0, viewCount + :delta) WHERE postId = :postId")
    suspend fun adjustViewCount(postId: String, delta: Int)

    @Query("UPDATE posts SET solved = :solved WHERE postId = :postId")
    suspend fun updatePostSolved(postId: String, solved: Boolean)

    @Query("DELETE FROM posts WHERE cachedAt < :threshold")
    suspend fun evictOldPosts(threshold: Long)
}

@Dao
interface ReplyDao {
    @Query("SELECT * FROM replies WHERE postId = :postId ORDER BY accepted DESC, upvotes DESC")
    fun observeRepliesForPost(postId: String): Flow<List<ReplyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplies(replies: List<ReplyEntity>)

    @Query("UPDATE replies SET body = :body WHERE postId = :postId AND replyId = :replyId")
    suspend fun updateReplyBody(postId: String, replyId: String, body: String)

    @Query("UPDATE replies SET accepted = :accepted WHERE postId = :postId AND replyId = :replyId")
    suspend fun updateReplyAccepted(postId: String, replyId: String, accepted: Boolean)

    @Query("DELETE FROM replies WHERE postId = :postId AND replyId = :replyId")
    suspend fun deleteReply(postId: String, replyId: String)

    @Query("DELETE FROM replies WHERE postId = :postId")
    suspend fun deleteRepliesForPost(postId: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun observeUser(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserById(uid: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET postCount = MAX(0, postCount + :delta) WHERE uid = :uid")
    suspend fun adjustPostCount(uid: String, delta: Int)
}
