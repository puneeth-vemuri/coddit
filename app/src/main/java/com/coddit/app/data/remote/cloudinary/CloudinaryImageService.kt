package com.coddit.app.data.remote.cloudinary

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.cloudinary.Cloudinary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudinaryImageService @Inject constructor(
    private val cloudinaryConfig: CloudinaryConfig,
    @ApplicationContext private val context: Context
) {
    private val cloudinary: Cloudinary
        get() = cloudinaryConfig.getCloudinary()
    
    suspend fun uploadPostImages(
        imageUris: List<Uri>,
        ownerUid: String,
        postId: String
    ): List<String> = withContext(Dispatchers.IO) {
        uploadImages(
            uris = imageUris,
            ownerUid = ownerUid,
            folder = "${CloudinaryConfig.FOLDER_POSTS}/$postId",
            transformation = CloudinaryConfig.POST_IMAGE_TRANSFORMATION
        )
    }
    
    suspend fun uploadReplyImages(
        imageUris: List<Uri>,
        ownerUid: String,
        postId: String,
        replyId: String
    ): List<String> = withContext(Dispatchers.IO) {
        uploadImages(
            uris = imageUris,
            ownerUid = ownerUid,
            folder = "${CloudinaryConfig.FOLDER_REPLIES}/$postId/$replyId",
            transformation = CloudinaryConfig.POST_IMAGE_TRANSFORMATION
        )
    }
    
    suspend fun uploadProfileImage(
        imageUri: Uri,
        ownerUid: String
    ): String = withContext(Dispatchers.IO) {
        uploadSingleImage(
            uri = imageUri,
            ownerUid = ownerUid,
            folder = CloudinaryConfig.FOLDER_AVATARS,
            transformation = CloudinaryConfig.AVATAR_TRANSFORMATION,
            publicId = "avatar_$ownerUid" // Use consistent public ID for avatars
        )
    }
    
    private suspend fun uploadImages(
        uris: List<Uri>,
        ownerUid: String,
        folder: String,
        transformation: String
    ): List<String> {
        if (uris.isEmpty()) return emptyList()
        
        return uris.map { uri ->
            uploadSingleImage(
                uri = uri,
                ownerUid = ownerUid,
                folder = folder,
                transformation = transformation
            )
        }
    }
    
    private suspend fun uploadSingleImage(
        uri: Uri,
        ownerUid: String,
        folder: String,
        transformation: String,
        publicId: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val options = mutableMapOf<String, Any>(
                "folder" to folder,
                "transformation" to transformation,
                "resource_type" to "image"
            )
            
            // Use ownerUid as part of public ID for organization
            val finalPublicId = publicId ?: "${ownerUid}_${UUID.randomUUID()}"
            options["public_id"] = finalPublicId
            
            // Read file bytes
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Cannot open input stream for URI: $uri")
            
            val bytes = inputStream.use { it.readBytes() }
            
            // Upload to Cloudinary
            val result = cloudinary.uploader().upload(bytes, options)
            
            val secureUrl = result["secure_url"] as? String
                ?: throw IllegalStateException("Upload succeeded but no URL returned")
            
            secureUrl
        } catch (e: Exception) {
            throw RuntimeException("Cloudinary upload failed: ${e.message}", e)
        }
    }
    
    suspend fun deleteImage(imageUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Extract public ID from Cloudinary URL
            val publicId = extractPublicIdFromUrl(imageUrl)
            if (publicId != null) {
                val result = cloudinary.uploader().destroy(publicId, emptyMap<String, Any>())
                result["result"] == "ok"
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun getOptimizedUrl(
        originalUrl: String,
        transformation: String = CloudinaryConfig.DEFAULT_TRANSFORMATION
    ): String {
        return if (isCloudinaryUrl(originalUrl)) {
            // Apply transformation to Cloudinary URL
            originalUrl.replace("/upload/", "/upload/$transformation/")
        } else {
            // For non-Cloudinary URLs (e.g., existing Firebase URLs), return as-is
            originalUrl
        }
    }
    
    fun getThumbnailUrl(imageUrl: String): String {
        return getOptimizedUrl(imageUrl, CloudinaryConfig.THUMBNAIL_TRANSFORMATION)
    }
    
    private fun isCloudinaryUrl(url: String): Boolean {
        return url.contains("res.cloudinary.com")
    }
    
    private fun extractPublicIdFromUrl(url: String): String? {
        if (!isCloudinaryUrl(url)) return null
        
        val pattern = """.*/([^/]+)\.[a-zA-Z]+$""".toRegex()
        val match = pattern.find(url)
        return match?.groupValues?.get(1)
    }
    
    // Optional: Image compression helper (can be used before upload)
    private fun compressImage(uri: Uri, maxSizeKB: Int = 500): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            if (options.outWidth <= 0 || options.outHeight <= 0) return null
            
            // Calculate sample size to reduce memory
            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, 2048, 2048)
            
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            
            val newInputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(newInputStream, null, decodeOptions)
            newInputStream.close()
            
            if (bitmap == null) return null
            
            var quality = 90
            var outputStream: ByteArrayOutputStream
            var bytes: ByteArray
            
            do {
                outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                bytes = outputStream.toByteArray()
                quality -= 10
            } while (bytes.size > maxSizeKB * 1024 && quality > 10)
            
            outputStream.close()
            bitmap.recycle()
            
            bytes
        } catch (e: Exception) {
            null
        }
    }
    
    private fun calculateSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var sampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / sampleSize >= reqHeight && halfWidth / sampleSize >= reqWidth) {
                sampleSize *= 2
            }
        }
        
        return sampleSize
    }
}