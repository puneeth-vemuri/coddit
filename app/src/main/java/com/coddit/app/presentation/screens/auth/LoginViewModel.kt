package com.coddit.app.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coddit.app.data.local.datastore.SessionDataStore
import com.coddit.app.domain.repository.UserRepository
import com.coddit.app.presentation.util.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FirebaseUser?>>(UiState.Empty)
    val uiState: StateFlow<UiState<FirebaseUser?>> = _uiState.asStateFlow()

    val onboardingCompleted: StateFlow<Boolean> = sessionDataStore.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                syncOnboardingFromProfile(user)
                _uiState.value = UiState.Success(user)
            }
        }
    }

    fun onSignInSuccess(user: FirebaseUser) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            sessionDataStore.saveSession(user.uid, user.displayName)
            syncOnboardingFromProfile(user)
            _uiState.value = UiState.Success(user)
        }
    }

    private suspend fun syncOnboardingFromProfile(user: FirebaseUser) {
        val existingProfile = withTimeoutOrNull(2000L) {
            userRepository.getUserProfile(user.uid)
                .filterNotNull()
                .first()
        }

        if (existingProfile != null) {
            sessionDataStore.saveSession(user.uid, existingProfile.username)
            sessionDataStore.setOnboardingCompleted(true)
        } else {
            sessionDataStore.setOnboardingCompleted(false)
        }
    }

    fun onSignInError(message: String) {
        _uiState.value = UiState.Error(message)
    }
}
