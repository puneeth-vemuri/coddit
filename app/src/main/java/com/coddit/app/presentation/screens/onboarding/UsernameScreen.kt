package com.coddit.app.presentation.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coddit.app.presentation.util.UiState
import kotlinx.coroutines.launch

@Composable
fun UsernameScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    val username by viewModel.username.collectAsState()
    val status by viewModel.usernameStatus.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose your username",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "This is how other builders will see you.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = viewModel::onUsernameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") },
            placeholder = { Text("e.g. kotlin_king") },
            singleLine = true,
            trailingIcon = {
                when (status) {
                    is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    is UiState.Success -> Icon(Icons.Default.CheckCircle, "Available", tint = Color.Green)
                    is UiState.Error -> Icon(Icons.Default.Error, "Unavailable", tint = MaterialTheme.colorScheme.error)
                    else -> null
                }
            },
            isError = status is UiState.Error
        )

        if (status is UiState.Error) {
            val errorMessage = (status as UiState.Error).message
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp)
            )
        } else if (status is UiState.Success) {
            Text(
                text = "Username available",
                color = Color(0xFF22C55E),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                scope.launch {
                    val result = viewModel.saveUsernameForCurrentUser()
                    if (result.isSuccess) {
                        onNext()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = status is UiState.Success && username.length >= 3
        ) {
            Text("Continue")
        }
    }
}
