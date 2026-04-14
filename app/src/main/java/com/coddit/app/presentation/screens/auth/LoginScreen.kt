package com.coddit.app.presentation.screens.auth

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.coddit.app.presentation.components.LoadingOverlay
import com.coddit.app.presentation.theme.CodditCard
import com.coddit.app.presentation.util.UiState
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.launch
import com.coddit.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.coddit.app.presentation.theme.CodditTeal
import com.coddit.app.presentation.theme.CodditDark
import com.coddit.app.presentation.theme.GithubPrimary

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    
    val credentialManager = CredentialManager.create(context)

    fun signInWithOAuthProvider(providerId: String) {
        val activity = context as? Activity
        if (activity == null) {
            viewModel.onSignInError("Unable to open $providerId sign-in")
            return
        }

        val auth = FirebaseAuth.getInstance()
        val provider = OAuthProvider.newBuilder(providerId)
        auth.startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    viewModel.onSignInSuccess(user)
                } else {
                    viewModel.onSignInError("$providerId sign-in returned no user")
                }
            }
            .addOnFailureListener { e ->
                viewModel.onSignInError(e.message ?: "$providerId sign-in failed")
            }
    }

    LaunchedEffect(uiState, onboardingCompleted) {
        if (uiState is UiState.Success) {
            onLoginSuccess(onboardingCompleted)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CodditDark)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(24.dp),
            color = CodditCard.copy(alpha = 0.42f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    modifier = Modifier.size(84.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = CodditTeal.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, CodditTeal.copy(alpha = 0.45f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = CodditTeal,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "<coddit/>",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = CodditTeal,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-1.5).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "where developers post problems and\nthe community ships solutions",
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    color = Color.White.copy(alpha = 0.62f)
                )

                Spacer(modifier = Modifier.height(36.dp))
                Button(
                    onClick = {
                        signInWithOAuthProvider("github.com")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GithubPrimary,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, CodditTeal.copy(alpha = 0.4f))
                ) {
                    Text("Continue with GitHub", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.08f))
                    Text(
                        text = "or",
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.08f))
                }

                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(context.getString(R.string.default_web_client_id))
                                    .setAutoSelectEnabled(false)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                val result = credentialManager.getCredential(
                                    context = context,
                                    request = request
                                )

                                val credential = result.credential
                                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                                    FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                        .addOnSuccessListener { authResult ->
                                            val user = authResult.user
                                            if (user != null) {
                                                viewModel.onSignInSuccess(user)
                                            } else {
                                                viewModel.onSignInError("Google sign-in returned no user")
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            viewModel.onSignInError(e.message ?: "Google sign-in failed")
                                        }
                                } else {
                                    viewModel.onSignInError("Unsupported Google credential response")
                                }
                            } catch (e: Exception) {
                                viewModel.onSignInError(e.message ?: "Google sign-in cancelled")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFFEA4335))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "by signing in you agree to our terms\ncoddit is ad-free · open to all devs",
                    fontSize = 10.sp,
                    lineHeight = 18.sp,
                    color = Color.White.copy(alpha = 0.32f)
                )

                if (uiState is UiState.Error) {
                    Text(
                        text = (uiState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 14.dp),
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (uiState is UiState.Loading) {
            LoadingOverlay()
        }
    }
}
