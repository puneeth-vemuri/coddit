package com.coddit.app.presentation.screens.onboarding

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun LinkAccountsScreen(
    viewModel: LinkAccountsViewModel = hiltViewModel(),
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    val linkedProviders by viewModel.linkedProviders.collectAsState()
    val linkingProvider by viewModel.linkingProvider.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Link your accounts",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Connect third-party OAuth accounts to build trust on your profile.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        listOf("Google", "GitHub").forEach { provider ->
            val isLinked = provider in linkedProviders
            val isLoading = linkingProvider == provider
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = if (isLinked) Icons.Default.Check else Icons.Default.Link,
                            contentDescription = null,
                            tint = if (isLinked) Color(0xFF22C55E) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = provider,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedButton(
                        enabled = !isLoading,
                        onClick = {
                            viewModel.clearError()
                            if (isLinked) {
                                viewModel.unlinkProvider(provider)
                            } else {
                                when (provider) {
                                    "Google" -> scope.launch { viewModel.linkGoogle(context) }
                                    "GitHub" -> {
                                        if (activity != null) {
                                            viewModel.linkGitHub(activity)
                                        } else {
                                            viewModel.clearError()
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        if (isLoading) {
                            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (isLinked) "Unlink" else "Link")
                        }
                    }
                }
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Continue")
        }

        TextButton(onClick = onSkip) {
            Text("Skip", fontWeight = FontWeight.SemiBold)
        }
    }
}
