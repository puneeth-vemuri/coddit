package com.coddit.app.presentation.screens.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coddit.app.domain.model.Post
import com.coddit.app.domain.usecase.post.CreatePostUseCase
import com.coddit.app.presentation.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<String>>(emptyList())
    val selectedImages: StateFlow<List<String>> = _selectedImages.asStateFlow()

    fun addImage(uri: String) {
        if (_selectedImages.value.size < 5) {
            _selectedImages.value = _selectedImages.value + uri
        }
    }

    fun removeImage(uri: String) {
        _selectedImages.value = _selectedImages.value - uri
    }

    fun createPost(
        authorUid: String,
        authorUsername: String,
        authorAvatarUrl: String?,
        title: String,
        body: String,
        tags: List<String>,
        imageUrls: List<String>
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val post = Post(
                postId = UUID.randomUUID().toString(),
                authorUid = authorUid,
                authorUsername = authorUsername,
                authorAvatarUrl = authorAvatarUrl,
                authorLinkedAccounts = emptyList(),
                title = title,
                body = body,
                codeSnippet = null,
                imageUrls = imageUrls,
                tags = tags,
                upvotes = 0,
                viewCount = 0,
                replyCount = 0,
                solved = false,
                acceptedReplyId = null,
                createdAt = System.currentTimeMillis()
            )
            
            val result = createPostUseCase(post)
            _uiState.value = if (result.isSuccess) {
                UiState.Success(Unit)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to post")
            }
        }
    }
}
