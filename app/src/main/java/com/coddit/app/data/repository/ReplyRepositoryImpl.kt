package com.coddit.app.data.repository

import com.coddit.app.data.local.db.dao.ReplyDao
import com.coddit.app.data.local.db.dao.PostDao
import com.coddit.app.data.remote.firestore.ReplyRemoteSource
import com.coddit.app.data.remote.storage.ImageStorageSource
import com.coddit.app.data.repository.Mappers.toDomain
import com.coddit.app.data.repository.Mappers.toEntity
import com.coddit.app.domain.model.Reply
import com.coddit.app.domain.repository.ReplyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReplyRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val replyDao: ReplyDao,
    private val replyRemoteSource: ReplyRemoteSource,
    private val imageStorageSource: ImageStorageSource
) : ReplyRepository {

    override fun getRepliesForPost(postId: String): Flow<List<Reply>> {
        return replyDao.observeRepliesForPost(postId)
            .map { entities -> entities.map { it.toDomain() } }
            .onStart {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val remote = replyRemoteSource.getReplies(postId)
                        replyDao.insertReplies(remote.map { it.toEntity() })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    override suspend fun createReply(reply: Reply): Result<Unit> {
        return try {
            val uploadedImageUrls = imageStorageSource.uploadReplyImages(
                imageRefs = reply.imageUrls,
                ownerUid = reply.authorUid,
                postId = reply.postId,
                replyId = reply.replyId
            )
            val replyWithUploadedImages = reply.copy(imageUrls = uploadedImageUrls)

            replyDao.insertReplies(listOf(replyWithUploadedImages.toEntity()))
            postDao.adjustReplyCount(reply.postId, 1)
            replyRemoteSource.saveReply(replyWithUploadedImages)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReply(postId: String, replyId: String, body: String): Result<Unit> {
        return try {
            replyDao.updateReplyBody(postId, replyId, body)
            replyRemoteSource.updateReply(postId, replyId, body)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReply(postId: String, replyId: String): Result<Unit> {
        return try {
            replyDao.deleteReply(postId, replyId)
            postDao.adjustReplyCount(postId, -1)
            replyRemoteSource.deleteReply(postId, replyId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptReply(postId: String, replyId: String): Result<Unit> {
        return try {
            // Update local database
            replyDao.updateReplyAccepted(postId, replyId, true)
            // Also mark the post as solved in local database
            postDao.updatePostSolved(postId, true)
            // Update Firestore
            replyRemoteSource.acceptReply(postId, replyId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun voteReply(postId: String, replyId: String, voterUid: String): Result<Unit> {
        return try {
            replyRemoteSource.voteReply(postId, replyId, 1) // Increment by 1
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
