package com.coddit.app.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coddit.app.domain.model.LinkedAccountProvider
import com.coddit.app.domain.model.Post
import com.coddit.app.domain.model.User
import com.coddit.app.domain.repository.UserRepository
import com.coddit.app.domain.usecase.feed.GetPostsByAuthorUseCase
import com.coddit.app.domain.usecase.user.FollowUserUseCase
import com.coddit.app.domain.usecase.user.GetUserProfileUseCase
import com.coddit.app.domain.usecase.user.IsFollowingUseCase
import com.coddit.app.domain.usecase.user.UnfollowUserUseCase
import com.coddit.app.presentation.util.UiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getPostsByAuthorUseCase: GetPostsByAuthorUseCase,
    private val userRepository: UserRepository,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase,
    private val isFollowingUseCase: IsFollowingUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private var currentUid: String? = null

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val uiState: StateFlow<UiState<User>> = _uiState.asStateFlow()

    private val _postsState = MutableStateFlow<UiState<List<Post>>>(UiState.Loading)
    val postsState: StateFlow<UiState<List<Post>>> = _postsState.asStateFlow()

    private val _isFollowingState = MutableStateFlow<Boolean?>(null)
    val isFollowingState: StateFlow<Boolean?> = _isFollowingState.asStateFlow()

    fun loadProfile(uid: String) {
        currentUid = uid
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getUserProfileUseCase(uid)
                .catch { e -> _uiState.value = UiState.Error(e.message ?: "Failed to load profile") }
                .collect { user ->
                    _uiState.value = if (user == null) UiState.Error("User not found") else UiState.Success(user)
                }
        }

        viewModelScope.launch {
            _postsState.value = UiState.Loading
            getPostsByAuthorUseCase(uid)
                .catch { e -> _postsState.value = UiState.Error(e.message ?: "Failed to load posts") }
                .collect { posts ->
                    _postsState.value = if (posts.isEmpty()) UiState.Empty else UiState.Success(posts)
                }
        }

        // Check follow status if viewing another user's profile
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null && currentUserUid != uid) {
            viewModelScope.launch {
                checkFollowingStatus(currentUserUid, uid)
            }
        } else {
            _isFollowingState.value = null // Own profile or not logged in
        }
    }

    private suspend fun checkFollowingStatus(followerUid: String, followedUid: String) {
        isFollowingUseCase(followerUid, followedUid).onSuccess { isFollowing ->
            _isFollowingState.value = isFollowing
        }.onFailure {
            _isFollowingState.value = false
        }
    }

    fun followUser() {
        val followerUid = auth.currentUser?.uid ?: return
        val followedUid = currentUid ?: return
        if (followerUid == followedUid) return

        viewModelScope.launch {
            followUserUseCase(followerUid, followedUid).onSuccess {
                _isFollowingState.value = true
                // Refresh user profile to update follower count
                loadProfile(followedUid)
            }
        }
    }

    fun unfollowUser() {
        val followerUid = auth.currentUser?.uid ?: return
        val followedUid = currentUid ?: return
        if (followerUid == followedUid) return

        viewModelScope.launch {
            unfollowUserUseCase(followerUid, followedUid).onSuccess {
                _isFollowingState.value = false
                // Refresh user profile to update follower count
                loadProfile(followedUid)
            }
        }
    }

    fun unlinkAccount(provider: LinkedAccountProvider) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            userRepository.unlinkAccount(uid, provider)
        }
    }

    fun updateAvatar(imageRef: String) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            userRepository.updateProfile(uid = uid, avatarRef = imageRef)
        }
    }

    fun updateSkills(skills: List<String>) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            userRepository.updateProfile(uid = uid, skills = skills)
        }
    }
}
