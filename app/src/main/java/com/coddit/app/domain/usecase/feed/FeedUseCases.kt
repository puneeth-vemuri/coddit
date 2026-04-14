package com.coddit.app.domain.usecase.feed

import com.coddit.app.domain.model.Post
import com.coddit.app.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFeedUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke(tags: List<String>): Flow<List<Post>> {
        return postRepository.getFeed(tags)
    }
}

class GetPostDetailUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke(postId: String): Flow<Post?> {
        return postRepository.getPostDetail(postId)
    }
}

class GetPostsByAuthorUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke(authorUid: String): Flow<List<Post>> {
        return postRepository.getPostsByAuthor(authorUid)
    }
}
