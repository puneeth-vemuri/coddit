package com.coddit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.coddit.app.presentation.MainViewModel
import com.coddit.app.presentation.components.LoadingOverlay
import com.coddit.app.presentation.navigation.NavGraph
import com.coddit.app.presentation.theme.CodditTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val startDestination by viewModel.startDestination.collectAsState()

            CodditTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (startDestination == null) {
                        LoadingOverlay()
                    } else {
                        NavGraph(
                            modifier = Modifier.padding(innerPadding),
                            startDestination = startDestination!!
                        )
                    }
                }
            }
        }
    }
}