package com.coddit.app.domain.repository

import com.coddit.app.domain.model.Post
import com.coddit.app.domain.model.Reply
import com.coddit.app.domain.model.User
import com.coddit.app.domain.model.SafeLink
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getFeed(tags: List<String>): Flow<List<Post>>
    fun getPostsByAuthor(authorUid: String): Flow<List<Post>>
    fun getPostDetail(postId: String): Flow<Post?>
    suspend fun createPost(post: Post): Result<Unit>
    suspend fun updatePost(postId: String, title: String, body: String, tags: List<String>, imageUrls: List<String>): Result<Unit>
    suspend fun votePost(postId: String, voterUid: String): Result<Unit>
    suspend fun incrementPostView(postId: String): Result<Unit>
    suspend fun deletePost(postId: String): Result<Unit>
}

interface ReplyRepository {
    fun getRepliesForPost(postId: String): Flow<List<Reply>>
    suspend fun createReply(reply: Reply): Result<Unit>
    suspend fun updateReply(postId: String, replyId: String, body: String): Result<Unit>
    suspend fun deleteReply(postId: String, replyId: String): Result<Unit>
    suspend fun acceptReply(postId: String, replyId: String): Result<Unit>
    suspend fun voteReply(postId: String, replyId: String, voterUid: String): Result<Unit>
}

interface UserRepository {
    fun getUserProfile(uid: String): Flow<User?>
    suspend fun checkUsernameAvailability(username: String): Result<Boolean>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun linkAccount(uid: String, account: com.coddit.app.domain.model.LinkedAccount): Result<Unit>
    suspend fun unlinkAccount(uid: String, provider: com.coddit.app.domain.model.LinkedAccountProvider): Result<Unit>
    suspend fun updateProfile(uid: String, avatarRef: String? = null, skills: List<String>? = null): Result<Unit>
    suspend fun updateBytes(uid: String, delta: Int): Result<Unit>
    suspend fun followUser(followerUid: String, followedUid: String): Result<Unit>
    suspend fun unfollowUser(followerUid: String, followedUid: String): Result<Unit>
    suspend fun isFollowing(followerUid: String, followedUid: String): Result<Boolean>
}

interface LinkSafetyRepository {
    suspend fun checkLinkSafety(url: String): SafeLink
}
