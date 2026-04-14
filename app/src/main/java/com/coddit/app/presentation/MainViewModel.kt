package com.coddit.app.presentation
 
import androidx.lifecycle.ViewModel
import com.coddit.app.data.local.datastore.SessionDataStore
import com.coddit.app.presentation.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        resolveStartDestination()
    }

    private fun resolveStartDestination() {
        val user = auth.currentUser
        if (user == null) {
            _startDestination.value = Screen.Login.route
        } else {
            // Check if onboarding is completed if logged in
            sessionDataStore.isOnboardingCompleted.onEach { completed ->
                _startDestination.value = if (completed) Screen.Feed.route else Screen.Username.route
            }.launchIn(kotlinx.coroutines.MainScope()) // Simplified for bypass fix
        }
    }
}
