package com.coddit.app.di

import com.coddit.app.data.repository.LinkSafetyRepositoryImpl
import com.coddit.app.domain.repository.LinkSafetyRepository
import com.coddit.app.data.repository.PostRepositoryImpl
import com.coddit.app.data.repository.ReplyRepositoryImpl
import com.coddit.app.data.repository.UserRepositoryImpl
import com.coddit.app.domain.repository.PostRepository
import com.coddit.app.domain.repository.ReplyRepository
import com.coddit.app.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindReplyRepository(
        replyRepositoryImpl: ReplyRepositoryImpl
    ): ReplyRepository

    @Binds
    @Singleton
    abstract fun bindLinkSafetyRepository(
        linkSafetyRepositoryImpl: LinkSafetyRepositoryImpl
    ): LinkSafetyRepository
}
