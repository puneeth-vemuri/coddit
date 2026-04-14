package com.coddit.app.data.remote.storage

import android.content.Context
import android.net.Uri
import com.coddit.app.data.remote.cloudinary.CloudinaryImageService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorageSource @Inject constructor(
    private val cloudinaryImageService: CloudinaryImageService,
    @ApplicationContext private val context: Context
) {

    suspend fun uploadPostImages(imageRefs: List<String>, ownerUid: String, postId: String): List<String> {
        return withContext(Dispatchers.IO) {
            // Filter out remote URLs (already uploaded images)
            val (remoteUrls, localUris) = imageRefs.partition { isRemoteUrl(it) }
            
            // Convert local URI strings to Uri objects
            val localUriList = localUris.map { Uri.parse(it) }
            
            // Upload local images to Cloudinary
            val cloudinaryUrls = if (localUriList.isNotEmpty()) {
                cloudinaryImageService.uploadPostImages(localUriList, ownerUid, postId)
            } else {
                emptyList()
            }
            
            // Combine remote URLs (already uploaded) with newly uploaded Cloudinary URLs
            remoteUrls + cloudinaryUrls
        }
    }

    suspend fun uploadReplyImages(imageRefs: List<String>, ownerUid: String, postId: String, replyId: String): List<String> {
        return withContext(Dispatchers.IO) {
            // Filter out remote URLs (already uploaded images)
            val (remoteUrls, localUris) = imageRefs.partition { isRemoteUrl(it) }
            
            // Convert local URI strings to Uri objects
            val localUriList = localUris.map { Uri.parse(it) }
            
            // Upload local images to Cloudinary
            val cloudinaryUrls = if (localUriList.isNotEmpty()) {
                cloudinaryImageService.uploadReplyImages(localUriList, ownerUid, postId, replyId)
            } else {
                emptyList()
            }
            
            // Combine remote URLs (already uploaded) with newly uploaded Cloudinary URLs
            remoteUrls + cloudinaryUrls
        }
    }

    suspend fun uploadProfileImage(imageRef: String, ownerUid: String): String {
        return withContext(Dispatchers.IO) {
            if (isRemoteUrl(imageRef)) {
                // Already a remote URL, return as-is
                imageRef
            } else {
                // Local URI, upload to Cloudinary
                val uri = Uri.parse(imageRef)
                cloudinaryImageService.uploadProfileImage(uri, ownerUid)
            }
        }
    }

    /**
     * Get optimized URL for display with Cloudinary transformations
     */
    fun getOptimizedUrl(imageUrl: String): String {
        return cloudinaryImageService.getOptimizedUrl(imageUrl)
    }

    /**
     * Get thumbnail URL for faster loading in lists
     */
    fun getThumbnailUrl(imageUrl: String): String {
        return cloudinaryImageService.getThumbnailUrl(imageUrl)
    }

    /**
     * Delete an image from Cloudinary storage
     */
    suspend fun deleteImage(imageUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            cloudinaryImageService.deleteImage(imageUrl)
        }
    }

    private fun isRemoteUrl(value: String): Boolean {
        return value.startsWith("https://") || value.startsWith("http://") || value.startsWith("gs://")
    }

    companion object {
        // Note: Compression is now handled by Cloudinary transformations
        // We keep these constants for backward compatibility if needed elsewhere
        private const val MAX_EDGE_PX = 1600
        private const val JPEG_QUALITY = 84
    }
}
