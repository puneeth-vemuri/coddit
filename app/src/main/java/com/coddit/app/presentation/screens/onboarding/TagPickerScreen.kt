package com.coddit.app.presentation.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TagPickerScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit
) {
    val selectedTags by viewModel.selectedTags.collectAsState()
    
    val tags = listOf(
        "Android", "Kotlin", "React", "Python", "JavaScript", 
        "ML", "DevOps", "Web", "iOS", "Backend", "Database", 
        "Security", "UI/UX", "Open Source", "Career"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Personalize your feed",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Select at least 3 topics to follow.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tags) { tag ->
                FilterChip(
                    selected = tag in selectedTags,
                    onClick = { viewModel.onTagToggle(tag) },
                    label = { Text(tag) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.completeOnboarding()
                onComplete()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = selectedTags.size >= 3
        ) {
            Text("Start coding")
        }

        TextButton(
            onClick = {
                viewModel.completeOnboarding()
                onComplete()
            }
        ) {
            Text("Skip", fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
