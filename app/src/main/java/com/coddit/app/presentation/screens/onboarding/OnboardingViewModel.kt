package com.coddit.app.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coddit.app.domain.model.LinkedAccount
import com.coddit.app.domain.model.LinkedAccountProvider
import com.coddit.app.data.local.datastore.SessionDataStore
import com.coddit.app.domain.model.User
import com.coddit.app.domain.repository.UserRepository
import com.coddit.app.presentation.util.UiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionDataStore: SessionDataStore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _usernameStatus = MutableStateFlow<UiState<Boolean>>(UiState.Empty)
    val usernameStatus: StateFlow<UiState<Boolean>> = _usernameStatus.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    init {
        // Debounced username availability check
        _username
            .debounce(500L)
            .filter { it.length >= 3 }
            .onEach { _usernameStatus.value = UiState.Loading }
            .flatMapLatest { userRepository.checkUsernameAvailability(it).asFlow() }
            .onEach { result ->
                _usernameStatus.value = result.fold(
                    onSuccess = { if (it) UiState.Success(true) else UiState.Error("Username taken") },
                    onFailure = { UiState.Error("Check failed") }
                )
            }.launchIn(viewModelScope)
    }

    fun onUsernameChange(newUsername: String) {
        if (newUsername.all { it.isLetterOrDigit() || it == '_' }) {
            _username.value = newUsername
        }
    }

    fun onTagToggle(tag: String) {
        _selectedTags.update { current ->
            if (tag in current) current - tag else current + tag
        }
    }

    suspend fun saveUsernameForCurrentUser(): Result<Unit> {
        val currentUser = auth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val pickedUsername = _username.value

        if (pickedUsername.length < 3) {
            return Result.failure(IllegalArgumentException("Username too short"))
        }

        val availability = userRepository.checkUsernameAvailability(pickedUsername)
        if (availability.isFailure) {
            return Result.failure(availability.exceptionOrNull() ?: IllegalStateException("Unable to check username"))
        }
        if (availability.getOrNull() != true) {
            return Result.failure(IllegalStateException("Username already exists"))
        }

        val user = User(
            uid = currentUser.uid,
            username = pickedUsername,
            displayName = currentUser.displayName ?: pickedUsername,
            avatarUrl = currentUser.photoUrl?.toString(),
            bytes = 0,
            postCount = 0,
            followerCount = 0,
            linkedAccounts = emptyList(),
            createdAt = System.currentTimeMillis()
        )

        val saveResult = userRepository.saveUser(user)
        if (saveResult.isSuccess) {
            syncInitialLinkedAccounts(currentUser.uid)
            sessionDataStore.saveSession(currentUser.uid, pickedUsername)
            _usernameStatus.value = UiState.Success(true)
        } else {
            val message = saveResult.exceptionOrNull()?.message ?: "Failed to save username"
            _usernameStatus.value = UiState.Error(message)
        }
        return saveResult
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            sessionDataStore.setOnboardingCompleted(true)
        }
    }

    fun completeOnboarding(uid: String, displayName: String?, avatarUrl: String?) {
        viewModelScope.launch {
            val user = User(
                uid = uid,
                username = _username.value,
                displayName = displayName ?: _username.value,
                avatarUrl = avatarUrl,
                bytes = 0,
                postCount = 0,
                followerCount = 0,
                linkedAccounts = emptyList(),
                createdAt = System.currentTimeMillis()
            )
            val result = userRepository.saveUser(user)
            if (result.isSuccess) {
                syncInitialLinkedAccounts(uid)
                sessionDataStore.setOnboardingCompleted(true)
            }
        }
    }

    private suspend fun syncInitialLinkedAccounts(uid: String) {
        val firebaseUser = auth.currentUser ?: return
        val accounts = firebaseUser.providerData.mapNotNull { providerInfo ->
            when (providerInfo.providerId) {
                "google.com" -> LinkedAccount(
                    provider = LinkedAccountProvider.GOOGLE,
                    handle = providerInfo.email ?: firebaseUser.email ?: uid.take(8),
                    profileUrl = "",
                    displayData = providerInfo.email ?: firebaseUser.email ?: "Google account linked",
                    verified = true
                )
                "github.com" -> LinkedAccount(
                    provider = LinkedAccountProvider.GITHUB,
                    handle = providerInfo.displayName ?: firebaseUser.displayName ?: uid.take(8),
                    profileUrl = "",
                    displayData = "Connected via GitHub OAuth",
                    verified = true
                )
                else -> null
            }
        }

        accounts.forEach { account ->
            userRepository.linkAccount(uid, account)
        }
    }

    // Helper extension
    private fun <T> Result<T>.asFlow(): Flow<Result<T>> = flow { emit(this@asFlow) }
}
