package com.coddit.app.presentation.screens.post

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.coddit.app.presentation.components.TagChip
import com.coddit.app.presentation.theme.CodditCard
import com.coddit.app.presentation.theme.CodditTeal
import com.coddit.app.presentation.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPostSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    val selectedImages by viewModel.selectedImages.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val authUser = FirebaseAuth.getInstance().currentUser
    val currentUid = authUser?.uid ?: "unknown_uid"
    val currentUsername = authUser?.displayName?.takeIf { it.isNotBlank() }
        ?: authUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: "anonymous"
    val suggestedTags = listOf("android", "kotlin", "firebase", "compose", "python", "react")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris -> uris.forEach { viewModel.addImage(it.toString()) } }
    )

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) onPostSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("new post", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = CodditCard,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White.copy(alpha = 0.82f))
                            }
                        }
                    }
                },
                actions = {
                    Text(
                        text = "@$currentUsername",
                        color = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.padding(end = 14.dp),
                        fontSize = 14.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            InputLabel("TITLE")
            GlassInput(value = title, onValueChange = { if (it.length <= 120) title = it }, placeholder = "Why does Kotlin Flow not emit on rotation?")

            Spacer(modifier = Modifier.height(14.dp))
            InputLabel("BODY")
            GlassInput(
                value = body,
                onValueChange = { body = it },
                placeholder = "StateFlow in ViewModel but UI stops collecting after screen rotates...",
                minHeight = 98.dp,
                multiline = true
            )

            Spacer(modifier = Modifier.height(14.dp))
            InputLabel("TAGS")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(3).forEach {
                    TagChip(tag = it)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("tags (comma separated)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CodditTeal,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.14f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(suggestedTags) { tag ->
                    val selected = tagsInput.split(",").map { it.trim().lowercase() }.contains(tag)
                    TagChip(
                        tag = tag,
                        isSelected = selected,
                        onClick = {
                            if (!selected) {
                                val updatedTags = tagsInput
                                    .split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                                    .plus(tag)
                                    .distinct()
                                    .take(5)
                                tagsInput = updatedTags.joinToString(", ")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            InputLabel("ATTACH")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AttachButton(icon = Icons.Default.Image, label = "image") {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }

            if (selectedImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedImages) { uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(82.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(onClick = { viewModel.removeImage(uri) }, modifier = Modifier.align(Alignment.TopEnd).size(22.dp)) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.55f), CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Helpful replies can turn this thread into accepted solutions and earn bytes for the people who jump in.",
                color = Color.White.copy(alpha = 0.48f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    viewModel.createPost(
                        authorUid = currentUid,
                        authorUsername = currentUsername,
                        authorAvatarUrl = authUser?.photoUrl?.toString(),
                        title = title,
                        body = body,
                        tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        imageUrls = selectedImages
                    )
                },
                enabled = title.isNotBlank() && body.isNotBlank() && uiState !is UiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.06f),
                    disabledContentColor = Color.White.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
            ) {
                Text("post to coddit", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            if (uiState is UiState.Error) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = (uiState as UiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun InputLabel(text: String) {
    Text(text = text, color = Color.White.copy(alpha = 0.58f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun GlassInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minHeight: androidx.compose.ui.unit.Dp = 54.dp,
    multiline: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        shape = RoundedCornerShape(12.dp),
        color = CodditCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            singleLine = !multiline,
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, lineHeight = 24.sp),
            cursorBrush = SolidColor(CodditTeal),
            decorationBox = { inner ->
                if (value.isBlank()) {
                    Text(text = placeholder, color = Color.White.copy(alpha = 0.34f), fontSize = 16.sp, lineHeight = 22.sp)
                }
                inner()
            }
        )
    }
}

@Composable
private fun AttachButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.07f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.72f), modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
        }
    }
}
