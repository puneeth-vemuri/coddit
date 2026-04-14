package com.coddit.app.presentation.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coddit.app.domain.model.Post
import com.coddit.app.domain.usecase.feed.GetFeedUseCase
import com.coddit.app.domain.usecase.post.VotePostUseCase
import com.coddit.app.presentation.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeedUseCase: GetFeedUseCase,
    private val votePostUseCase: VotePostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Post>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Post>>> = _uiState.asStateFlow()

    private val _selectedTags = MutableStateFlow<List<String>>(emptyList())
    val selectedTags: StateFlow<List<String>> = _selectedTags.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getFeedUseCase(_selectedTags.value)
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { posts ->
                    _uiState.value = if (posts.isEmpty()) UiState.Empty else UiState.Success(posts)
                }
        }
    }

    fun onTagSelected(tag: String) {
        _selectedTags.update { current ->
            when {
                tag == "all" -> emptyList()
                tag in current -> current - tag
                else -> (current - "all") + tag
            }
        }
        loadFeed() // Reload with new filters
    }

    fun onRefresh() {
        loadFeed()
    }

    fun onVotePost(postId: String, voterUid: String) {
        viewModelScope.launch {
            votePostUseCase(postId, voterUid)
        }
    }
}
