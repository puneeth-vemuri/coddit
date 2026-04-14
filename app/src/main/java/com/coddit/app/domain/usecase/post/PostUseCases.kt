package com.coddit.app.domain.usecase.post

import com.coddit.app.domain.model.Post
import com.coddit.app.domain.repository.PostRepository
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(post: Post): Result<Unit> {
        return postRepository.createPost(post)
    }
}

class VotePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, voterUid: String): Result<Unit> {
        return postRepository.votePost(postId, voterUid)
    }
}

class IncrementPostViewUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return postRepository.incrementPostView(postId)
    }
}

class DeletePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return postRepository.deletePost(postId)
    }
}

class UpdatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, title: String, body: String, tags: List<String>, imageUrls: List<String>): Result<Unit> {
        return postRepository.updatePost(postId, title, body, tags, imageUrls)
    }
}
