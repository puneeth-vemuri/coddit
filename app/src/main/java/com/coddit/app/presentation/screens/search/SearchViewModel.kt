package com.coddit.app.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coddit.app.domain.model.Post
import com.coddit.app.domain.usecase.feed.GetFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getFeedUseCase: GetFeedUseCase
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            getFeedUseCase(emptyList()).collect { fetched ->
                _posts.value = fetched
            }
        }
    }
}
