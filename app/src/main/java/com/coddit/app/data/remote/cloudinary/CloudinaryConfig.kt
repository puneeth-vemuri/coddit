package com.coddit.app.data.remote.cloudinary

import android.content.Context
import com.cloudinary.Cloudinary
import com.coddit.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudinaryConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _cloudinary: Cloudinary by lazy {
        // Initialize Cloudinary with configuration from BuildConfig
        val config = mutableMapOf<String, Any>()
        
        // Get credentials from BuildConfig (set in build.gradle.kts from local.properties)
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val apiKey = BuildConfig.CLOUDINARY_API_KEY
        val apiSecret = BuildConfig.CLOUDINARY_API_SECRET
        
        if (cloudName.isNotEmpty() && apiKey.isNotEmpty() && apiSecret.isNotEmpty()) {
            config["cloud_name"] = cloudName
            config["api_key"] = apiKey
            config["api_secret"] = apiSecret
            config["secure"] = true
        } else {
            // If credentials are not set, use a placeholder (will fail at runtime)
            // This helps with development/testing without actual Cloudinary account
            config["cloud_name"] = "development"
            config["api_key"] = "test"
            config["api_secret"] = "test"
            config["secure"] = true
        }
        
        Cloudinary(config)
    }
    
    fun getCloudinary(): Cloudinary {
        return _cloudinary
    }
    
    companion object {
        // Default transformation options for optimized images
        const val DEFAULT_TRANSFORMATION = "q_auto,f_auto,w_auto,dpr_auto"
        const val AVATAR_TRANSFORMATION = "q_auto,f_auto,w_200,h_200,c_fill,g_face"
        const val POST_IMAGE_TRANSFORMATION = "q_auto,f_auto,w_1200"
        const val THUMBNAIL_TRANSFORMATION = "q_auto,f_auto,w_400"
        
        // Folder structure
        const val FOLDER_AVATARS = "coddit/avatars"
        const val FOLDER_POSTS = "coddit/posts"
        const val FOLDER_REPLIES = "coddit/replies"
    }
}