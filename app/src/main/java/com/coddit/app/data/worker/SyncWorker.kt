package com.coddit.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.coddit.app.data.local.db.dao.PostDao
import com.coddit.app.data.remote.firestore.PostRemoteSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val postDao: PostDao,
    private val postRemoteSource: PostRemoteSource
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // 1. Fetch fresh posts
            val remotePosts = postRemoteSource.getFeed(emptyList())
            
            // 2. Update local DB
            // postDao.insertPosts(remotePosts.map { it.toEntity() })
            
            // 3. Evict old cached data
            postDao.evictOldPosts(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L))

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
