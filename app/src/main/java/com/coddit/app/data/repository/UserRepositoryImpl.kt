package com.coddit.app.data.repository

import com.coddit.app.data.local.db.dao.UserDao
import com.coddit.app.data.remote.firestore.UserRemoteSource
import com.coddit.app.data.remote.firestore.PostRemoteSource
import com.coddit.app.data.remote.storage.ImageStorageSource
import com.coddit.app.data.repository.Mappers.toDomain
import com.coddit.app.data.repository.Mappers.toEntity
import com.coddit.app.domain.model.User
import com.coddit.app.domain.model.LinkedAccount
import com.coddit.app.domain.model.LinkedAccountProvider
import com.coddit.app.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userRemoteSource: UserRemoteSource,
    private val postRemoteSource: PostRemoteSource,
    private val imageStorageSource: ImageStorageSource
) : UserRepository {

    override fun getUserProfile(uid: String): Flow<User?> {
        return userDao.observeUser(uid)
            .map { it?.toDomain() }
            .onStart {
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching {
                        userRemoteSource.getUser(uid)
                    }.getOrNull()?.let { remoteUser ->
                        userDao.insertUser(remoteUser.toEntity())
                    }
                }
            }
    }

    override suspend fun getFollowers(uid: String): Result<List<User>> {
        return try {
            Result.success(userRemoteSource.getFollowers(uid))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkUsernameAvailability(username: String): Result<Boolean> {
        return try {
            val available = userRemoteSource.isUsernameAvailable(username)
            Result.success(available)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        return try {
            userDao.insertUser(user.toEntity())
            userRemoteSource.saveUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkAccount(uid: String, account: LinkedAccount): Result<Unit> {
        return try {
            userRemoteSource.linkAccount(uid, account)
            userRemoteSource.getUser(uid)?.let { userDao.insertUser(it.toEntity()) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlinkAccount(uid: String, provider: LinkedAccountProvider): Result<Unit> {
        return try {
            userRemoteSource.unlinkAccount(uid, provider)
            userRemoteSource.getUser(uid)?.let { userDao.insertUser(it.toEntity()) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(uid: String, avatarRef: String?, skills: List<String>?): Result<Unit> {
        return try {
            val currentUser = userDao.getUserById(uid)?.toDomain()
                ?: userRemoteSource.getUser(uid)
                ?: return Result.failure(IllegalStateException("User not found"))

            val uploadedAvatarUrl = when {
                avatarRef.isNullOrBlank() -> currentUser.avatarUrl
                avatarRef.startsWith("http://") || avatarRef.startsWith("https://") || avatarRef.startsWith("gs://") -> avatarRef
                else -> imageStorageSource.uploadProfileImage(avatarRef, uid)
            }

            val updatedUser = currentUser.copy(
                avatarUrl = uploadedAvatarUrl,
                skills = skills ?: currentUser.skills
            )

            userDao.insertUser(updatedUser.toEntity())
            userRemoteSource.updateProfile(uid = uid, avatarUrl = uploadedAvatarUrl, skills = skills)
            
            // Update avatar in all existing posts by this user
            if (uploadedAvatarUrl != currentUser.avatarUrl) {
                postRemoteSource.updateAuthorAvatar(uid, uploadedAvatarUrl)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBytes(uid: String, delta: Int): Result<Unit> {
        return try {
            // Update remote
            userRemoteSource.updateBytes(uid, delta)
            
            // Update local cache
            val currentUser = userDao.getUserById(uid)?.toDomain()
                ?: userRemoteSource.getUser(uid)
                ?: return Result.failure(IllegalStateException("User not found"))
            
            val updatedUser = currentUser.copy(bytes = currentUser.bytes + delta)
            userDao.insertUser(updatedUser.toEntity())
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun followUser(followerUid: String, followedUid: String): Result<Unit> {
        return try {
            userRemoteSource.followUser(followerUid, followedUid)
            
            // Update local cache for followed user's follower count
            val followedUser = userDao.getUserById(followedUid)?.toDomain()
                ?: userRemoteSource.getUser(followedUid)
            followedUser?.let {
                val updatedUser = it.copy(followerCount = it.followerCount + 1)
                userDao.insertUser(updatedUser.toEntity())
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unfollowUser(followerUid: String, followedUid: String): Result<Unit> {
        return try {
            userRemoteSource.unfollowUser(followerUid, followedUid)
            
            // Update local cache for followed user's follower count
            val followedUser = userDao.getUserById(followedUid)?.toDomain()
                ?: userRemoteSource.getUser(followedUid)
            followedUser?.let {
                val updatedUser = it.copy(followerCount = (it.followerCount - 1).coerceAtLeast(0))
                userDao.insertUser(updatedUser.toEntity())
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isFollowing(followerUid: String, followedUid: String): Result<Boolean> {
        return try {
            val isFollowing = userRemoteSource.isFollowing(followerUid, followedUid)
            Result.success(isFollowing)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
