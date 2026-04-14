package com.coddit.app.di

import android.content.Context
import com.coddit.app.data.remote.cloudinary.CloudinaryConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    @Provides
    @Singleton
    fun provideCloudinaryConfig(@ApplicationContext context: Context): CloudinaryConfig {
        return CloudinaryConfig(context)
    }
}