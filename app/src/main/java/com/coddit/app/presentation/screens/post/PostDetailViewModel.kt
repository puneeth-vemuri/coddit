package com.coddit.app.presentation.screens.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coddit.app.domain.model.Post
import com.coddit.app.domain.model.Reply
import com.coddit.app.domain.usecase.feed.GetPostDetailUseCase
import com.coddit.app.domain.usecase.reply.AcceptReplyUseCase
import com.coddit.app.domain.usecase.reply.CreateReplyUseCase
import com.coddit.app.domain.usecase.reply.DeleteReplyUseCase
import com.coddit.app.domain.usecase.reply.UpdateReplyUseCase
import com.coddit.app.domain.usecase.reply.VoteReplyUseCase
import com.coddit.app.domain.usecase.post.DeletePostUseCase
import com.coddit.app.domain.usecase.post.IncrementPostViewUseCase
import com.coddit.app.domain.usecase.post.UpdatePostUseCase
import com.coddit.app.domain.usecase.post.VotePostUseCase
import com.coddit.app.domain.repository.ReplyRepository
import com.coddit.app.presentation.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val getPostDetailUseCase: GetPostDetailUseCase,
    private val replyRepository: ReplyRepository,
    private val createReplyUseCase: CreateReplyUseCase,
    private val acceptReplyUseCase: AcceptReplyUseCase,
    private val voteReplyUseCase: VoteReplyUseCase,
    private val updateReplyUseCase: UpdateReplyUseCase,
    private val deleteReplyUseCase: DeleteReplyUseCase,
    private val votePostUseCase: VotePostUseCase,
    private val incrementPostViewUseCase: IncrementPostViewUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val deletePostUseCase: DeletePostUseCase
) : ViewModel() {

    private val _postState = MutableStateFlow<UiState<Post>>(UiState.Loading)
    val postState: StateFlow<UiState<Post>> = _postState.asStateFlow()

    private val _repliesState = MutableStateFlow<UiState<List<Reply>>>(UiState.Loading)
    val repliesState: StateFlow<UiState<List<Reply>>> = _repliesState.asStateFlow()

    private val _replyImages = MutableStateFlow<List<String>>(emptyList())
    val replyImages: StateFlow<List<String>> = _replyImages.asStateFlow()

    private val _operationState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val operationState: StateFlow<UiState<Unit>> = _operationState.asStateFlow()

    private val _postDeleted = MutableStateFlow(false)
    val postDeleted: StateFlow<Boolean> = _postDeleted.asStateFlow()

    fun addReplyImage(uri: String) {
        if (_replyImages.value.size < 5) {
            _replyImages.update { it + uri }
        }
    }

    fun removeReplyImage(uri: String) {
        _replyImages.update { it - uri }
    }

    fun loadPost(postId: String) {
        viewModelScope.launch {
            incrementPostViewUseCase(postId)
        }

        viewModelScope.launch {
            getPostDetailUseCase(postId).collect { post ->
                _postState.value = if (post == null) UiState.Error("Post not found") else UiState.Success(post)
            }
        }
        
        viewModelScope.launch {
            replyRepository.getRepliesForPost(postId).collect { replies ->
                _repliesState.value = if (replies.isEmpty()) UiState.Empty else UiState.Success(replies)
            }
        }
    }

    fun onAcceptReply(postId: String, replyId: String) {
        viewModelScope.launch {
            acceptReplyUseCase(postId, replyId)
        }
    }

    fun onVoteReply(postId: String, replyId: String, voterUid: String) {
        viewModelScope.launch {
            voteReplyUseCase(postId, replyId, voterUid)
        }
    }

    fun onVotePost(postId: String, voterUid: String) {
        viewModelScope.launch {
            votePostUseCase(postId, voterUid)
        }
    }

    fun onPostReply(postId: String, authorUid: String, authorUsername: String, authorAvatarUrl: String?, body: String) {
        viewModelScope.launch {
            val reply = Reply(
                replyId = UUID().toString(),
                postId = postId,
                authorUid = authorUid,
                authorUsername = authorUsername,
                authorAvatarUrl = authorAvatarUrl,
                authorLinkedAccounts = emptyList(),
                body = body,
                imageUrls = _replyImages.value,
                links = emptyList(),
                accepted = false,
                upvotes = 0,
                createdAt = System.currentTimeMillis()
            )
            createReplyUseCase(reply)
            _replyImages.value = emptyList() // Clear drafting images
        }
    }

    fun onEditPost(postId: String, title: String, body: String, tags: List<String>, imageUrls: List<String>) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val result = updatePostUseCase(postId, title, body, tags, imageUrls)
            _operationState.value = if (result.isSuccess) UiState.Success(Unit) else UiState.Error(result.exceptionOrNull()?.message ?: "Failed to update post")
        }
    }

    fun onDeletePost(postId: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val result = deletePostUseCase(postId)
            if (result.isSuccess) {
                _postDeleted.value = true
                _operationState.value = UiState.Success(Unit)
            } else {
                _operationState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete post")
            }
        }
    }

    fun onEditReply(postId: String, replyId: String, body: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val result = updateReplyUseCase(postId, replyId, body)
            _operationState.value = if (result.isSuccess) UiState.Success(Unit) else UiState.Error(result.exceptionOrNull()?.message ?: "Failed to update reply")
        }
    }

    fun onDeleteReply(postId: String, replyId: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val result = deleteReplyUseCase(postId, replyId)
            _operationState.value = if (result.isSuccess) UiState.Success(Unit) else UiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete reply")
        }
    }
}

// Helper for UUID if not available
private fun UUID() = java.util.UUID.randomUUID()
