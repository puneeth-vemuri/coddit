package com.coddit.app.data.repository

import com.coddit.app.data.local.db.dao.PostDao
import com.coddit.app.data.local.db.dao.UserDao
import com.coddit.app.data.remote.firestore.PostRemoteSource
import com.coddit.app.data.remote.storage.ImageStorageSource
import com.coddit.app.data.repository.Mappers.toDomain
import com.coddit.app.data.repository.Mappers.toEntity
import com.coddit.app.domain.model.Post
import com.coddit.app.domain.repository.PostRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val postRemoteSource: PostRemoteSource,
    private val imageStorageSource: ImageStorageSource
) : PostRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override fun getFeed(tags: List<String>): Flow<List<Post>> {
        return postDao.observeAllPosts()
            .map { entities -> entities.map { it.toDomain() } }
            .onStart {
                // Background refresh
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val remote = postRemoteSource.getFeed(tags)
                        postDao.insertPosts(remote.map { it.toEntity() })
                        // Optional: evict old posts
                        postDao.evictOldPosts(System.currentTimeMillis() - (24 * 60 * 60 * 1000L))
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }
            }
    }

    override fun getPostsByAuthor(authorUid: String): Flow<List<Post>> {
        return postDao.observePostsByAuthor(authorUid)
            .map { entities -> entities.map { it.toDomain() } }
            .onStart {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val remote = postRemoteSource.getPostsByAuthor(authorUid)
                        postDao.insertPosts(remote.map { it.toEntity() })
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }
            }
    }

    override fun getPostDetail(postId: String): Flow<Post?> {
        return postDao.observeAllPosts() // Filtered local flow
            .map { posts -> posts.find { it.postId == postId }?.toDomain() }
    }

    override suspend fun createPost(post: Post): Result<Unit> {
        return try {
            val uploadedImageUrls = imageStorageSource.uploadPostImages(
                imageRefs = post.imageUrls,
                ownerUid = post.authorUid,
                postId = post.postId
            )
            val postWithUploadedImages = post.copy(imageUrls = uploadedImageUrls)

            postDao.insertPosts(listOf(postWithUploadedImages.toEntity()))
            userDao.adjustPostCount(post.authorUid, 1)
            postRemoteSource.savePost(postWithUploadedImages)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun votePost(postId: String, voterUid: String): Result<Unit> {
        return try {
            postRemoteSource.votePost(postId, 1) // Increment by 1
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun incrementPostView(postId: String): Result<Unit> {
        return try {
            postDao.adjustViewCount(postId, 1)
            postRemoteSource.incrementViewCount(postId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePost(postId: String, title: String, body: String, tags: List<String>, imageUrls: List<String>): Result<Unit> {
        return try {
            val ownerUid = (postDao.getPostById(postId)?.authorUid).orEmpty()
            val uploadedImageUrls = if (ownerUid.isNotBlank()) {
                imageStorageSource.uploadPostImages(
                    imageRefs = imageUrls,
                    ownerUid = ownerUid,
                    postId = postId
                )
            } else {
                imageUrls
            }

            postDao.updatePostContent(
                postId = postId,
                title = title,
                body = body,
                tagsJson = json.encodeToString(tags),
                imageUrlsJson = json.encodeToString(uploadedImageUrls)
            )
            postRemoteSource.updatePost(postId, title, body, tags, uploadedImageUrls)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val authorUid = postDao.getPostById(postId)?.authorUid
            postDao.deletePostById(postId)
            if (!authorUid.isNullOrBlank()) {
                userDao.adjustPostCount(authorUid, -1)
            }
            postRemoteSource.deletePost(postId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
