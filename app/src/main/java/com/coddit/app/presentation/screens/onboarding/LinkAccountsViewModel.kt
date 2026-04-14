package com.coddit.app.presentation.screens.onboarding

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coddit.app.R
import com.coddit.app.domain.model.LinkedAccount
import com.coddit.app.domain.model.LinkedAccountProvider
import com.coddit.app.domain.repository.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class LinkAccountsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _linkedProviders = MutableStateFlow<Set<String>>(emptySet())
    val linkedProviders: StateFlow<Set<String>> = _linkedProviders.asStateFlow()

    private val _linkingProvider = MutableStateFlow<String?>(null)
    val linkingProvider: StateFlow<String?> = _linkingProvider.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val httpClient = OkHttpClient()

    init {
        refreshLinkedProviders()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun refreshLinkedProviders() {
        val user = auth.currentUser
        if (user == null) {
            _linkedProviders.value = emptySet()
            return
        }

        val mapped = user.providerData.mapNotNull { info ->
            when (info.providerId) {
                "google.com" -> "Google"
                "github.com" -> "GitHub"
                else -> null
            }
        }.toSet()

        _linkedProviders.value = mapped
    }

    fun linkGitHub(activity: Activity) {
        val user = auth.currentUser
        if (user == null) {
            _errorMessage.value = "Sign in first to link accounts"
            return
        }

        _errorMessage.value = null
        _linkingProvider.value = "GitHub"

        val provider = OAuthProvider.newBuilder("github.com").build()
        user.startActivityForLinkWithProvider(activity, provider)
            .addOnSuccessListener { authResult ->
                viewModelScope.launch {
                    val profile = authResult.additionalUserInfo?.profile
                    persistLinkedAccount(user.uid, LinkedAccountProvider.GITHUB, user, profile)
                    refreshLinkedProviders()
                    _linkingProvider.value = null
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message ?: "GitHub linking failed"
                _linkingProvider.value = null
            }
    }

    fun linkGoogle(context: Context) {
        val user = auth.currentUser
        if (user == null) {
            _errorMessage.value = "Sign in first to link accounts"
            return
        }

        _errorMessage.value = null
        _linkingProvider.value = "Google"

        viewModelScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                    user.linkWithCredential(firebaseCredential).await()
                    persistLinkedAccount(user.uid, LinkedAccountProvider.GOOGLE, user)
                    refreshLinkedProviders()
                } else {
                    _errorMessage.value = "Unsupported Google credential response"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Google linking failed"
            } finally {
                _linkingProvider.value = null
            }
        }
    }

    fun unlinkProvider(provider: String) {
        val user = auth.currentUser
        if (user == null) {
            _errorMessage.value = "Sign in first to unlink accounts"
            return
        }

        val firebaseProviderId = when (provider) {
            "Google" -> "google.com"
            "GitHub" -> "github.com"
            else -> null
        }
        val domainProvider = when (provider) {
            "Google" -> LinkedAccountProvider.GOOGLE
            "GitHub" -> LinkedAccountProvider.GITHUB
            else -> null
        }

        if (firebaseProviderId == null || domainProvider == null) {
            _errorMessage.value = "Unsupported provider"
            return
        }

        _errorMessage.value = null
        _linkingProvider.value = provider

        viewModelScope.launch {
            try {
                user.unlink(firebaseProviderId).await()
                userRepository.unlinkAccount(user.uid, domainProvider)
                refreshLinkedProviders()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to unlink $provider"
            } finally {
                _linkingProvider.value = null
            }
        }
    }

    private suspend fun persistLinkedAccount(
        uid: String,
        provider: LinkedAccountProvider,
        user: com.google.firebase.auth.FirebaseUser,
        oauthProfile: Map<String, Any>? = null
    ) {
        val githubLogin = oauthProfile?.get("login")?.toString()
        val githubRepos = (oauthProfile?.get("public_repos") as? Number)?.toInt()
        val githubFollowers = (oauthProfile?.get("followers") as? Number)?.toInt()
        val githubStars = githubLogin?.let { fetchGitHubTotalStars(it) }

        val handle = when (provider) {
            LinkedAccountProvider.GITHUB -> githubLogin ?: user.displayName ?: user.email ?: uid.take(8)
            LinkedAccountProvider.GOOGLE -> user.email ?: user.displayName ?: uid.take(8)
            else -> user.displayName ?: user.email ?: uid.take(8)
        }

        val displayData = when (provider) {
            LinkedAccountProvider.GITHUB -> {
                when {
                    githubRepos != null && githubStars != null -> "$githubRepos repos · $githubStars stars"
                    githubRepos != null && githubFollowers != null -> "$githubRepos repos · $githubFollowers followers"
                    else -> "Connected via GitHub OAuth"
                }
            }
            LinkedAccountProvider.GOOGLE -> "${user.email ?: "Google account linked"}"
            else -> "Account linked"
        }

        val account = LinkedAccount(
            provider = provider,
            handle = handle,
            profileUrl = "",
            displayData = displayData,
            verified = true
        )

        userRepository.linkAccount(uid, account)
    }

    private suspend fun fetchGitHubTotalStars(username: String): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api.github.com/users/$username/repos?per_page=100")
                    .header("Accept", "application/vnd.github+json")
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body?.string() ?: return@withContext null
                    val repos = JSONArray(body)
                    var totalStars = 0
                    for (i in 0 until repos.length()) {
                        totalStars += repos.optJSONObject(i)?.optInt("stargazers_count", 0) ?: 0
                    }
                    totalStars
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
