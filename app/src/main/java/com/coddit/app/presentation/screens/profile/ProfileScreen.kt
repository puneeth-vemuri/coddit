package com.coddit.app.presentation.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coddit.app.domain.model.LinkedAccount
import com.coddit.app.domain.model.User
import com.coddit.app.presentation.components.LinkedAccountBadge
import com.coddit.app.presentation.components.PostCard
import com.coddit.app.presentation.components.TagChip
import com.coddit.app.presentation.components.UserAvatar
import com.coddit.app.presentation.theme.BytesPurple
import com.coddit.app.presentation.theme.CodditCard
import com.coddit.app.presentation.theme.CodditTeal
import com.coddit.app.presentation.theme.SolvedGreen
import com.coddit.app.presentation.util.UiState
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uid: String,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLinkAccount: () -> Unit,
    onPostClick: (String) -> Unit,
    onUserProfileClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val postsState by viewModel.postsState.collectAsState()
    val followersState by viewModel.followersState.collectAsState()
    val isOwnProfile = FirebaseAuth.getInstance().currentUser?.uid == uid
    var menuExpanded by remember { mutableStateOf(false) }
    var editingSkills by remember { mutableStateOf(false) }
    var showFollowersSheet by remember { mutableStateOf(false) }
    val followersSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uid) {
        viewModel.loadProfile(uid)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.updateAvatar(it.toString()) }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "profile",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = CodditCard,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White.copy(alpha = 0.82f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (isOwnProfile) {
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CodditCard, RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Default.MoreHoriz, contentDescription = "Actions", tint = Color.White.copy(alpha = 0.72f))
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Edit profile photo") },
                                    onClick = {
                                        menuExpanded = false
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit skills and interests") },
                                    onClick = {
                                        menuExpanded = false
                                        editingSkills = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Link accounts") },
                                    onClick = {
                                        menuExpanded = false
                                        onLinkAccount()
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CodditTeal)
                }
            }

            is UiState.Success -> {
                val user = state.data
                val postCount = when (val posts = postsState) {
                    is UiState.Success -> posts.data.size
                    is UiState.Empty -> 0
                    else -> user.postCount
                }
                val skills = if (user.skills.isNotEmpty()) user.skills else listOf("android", "kotlin", "flow", "jetpack")
                var skillsDraft by remember(user.uid, user.skills) {
                    mutableStateOf(user.skills.joinToString(", "))
                }

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        UserAvatar(url = user.avatarUrl, size = 64)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = user.displayName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "@${user.username} | building on Coddit", fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 26.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStatItem(label = "posts", value = "$postCount")
                            VerticalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.height(38.dp))
                            ProfileStatItem(label = "bytes", value = "${user.bytes}", valueColor = BytesPurple)
                            VerticalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.height(38.dp))
                            ProfileStatItem(
                                label = "followers",
                                value = "${user.followerCount}",
                                onClick = {
                                    showFollowersSheet = true
                                    viewModel.loadFollowers(uid)
                                }
                            )
                        }

                        // Follow button (only show if not own profile)
                        if (!isOwnProfile) {
                            val isFollowingState by viewModel.isFollowingState.collectAsState()
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    if (isFollowingState == true) {
                                        viewModel.unfollowUser()
                                    } else {
                                        viewModel.followUser()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.6f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowingState == true) Color.Transparent else CodditTeal,
                                    contentColor = if (isFollowingState == true) Color.White.copy(alpha = 0.7f) else Color.White
                                ),
                                border = if (isFollowingState == true) BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)) else null,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isFollowingState == true) "Following" else "Follow",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(18.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("LINKED ACCOUNTS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.55f))
                            Spacer(modifier = Modifier.height(12.dp))

                            user.linkedAccounts.forEach { account ->
                                LinkedAccountItem(
                                    account = account,
                                    showUnlink = isOwnProfile,
                                    onUnlink = { viewModel.unlinkAccount(account.provider) }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("SKILLS AND INTERESTS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.52f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                skills.forEach { skill ->
                                    TagChip(tag = skill)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("POSTS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.62f))
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    when (val posts = postsState) {
                        is UiState.Success -> {
                            items(posts.data) { post ->
                                PostCard(
                                    post = post,
                                    onClick = { onPostClick(post.postId) },
                                    onUpvote = {},
                                    onShare = {},
                                    onAuthorClick = { onUserProfileClick(post.authorUid) }
                                )
                            }
                        }

                        is UiState.Empty -> {
                            item {
                                Text(
                                    text = "No posts yet",
                                    color = Color.White.copy(alpha = 0.45f),
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }

                        is UiState.Loading -> {
                            item {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = CodditTeal
                                )
                            }
                        }

                        else -> Unit
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }

                if (editingSkills) {
                    AlertDialog(
                        onDismissRequest = { editingSkills = false },
                        title = { Text("Edit skills and interests") },
                        text = {
                            OutlinedTextField(
                                value = skillsDraft,
                                onValueChange = { skillsDraft = it },
                                label = { Text("Comma separated skills") },
                                placeholder = { Text("android, kotlin, firebase") }
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val updatedSkills = skillsDraft.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() }
                                        .distinct()
                                        .take(8)
                                    viewModel.updateSkills(updatedSkills)
                                    editingSkills = false
                                }
                            ) { Text("Save") }
                        },
                        dismissButton = {
                            TextButton(onClick = { editingSkills = false }) { Text("Cancel") }
                        }
                    )
                }
            }

            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "profile sync error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> Unit
        }

        if (showFollowersSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFollowersSheet = false },
                sheetState = followersSheetState,
                containerColor = CodditCard
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Followers",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    when (val followers = followersState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = CodditTeal)
                            }
                        }
                        is UiState.Success -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(followers.data, key = { it.uid }) { follower ->
                                    FollowerRow(
                                        follower = follower,
                                        onClick = {
                                            showFollowersSheet = false
                                            onUserProfileClick(follower.uid)
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                            }
                        }
                        is UiState.Empty -> {
                            Text(
                                text = "No followers yet",
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                        is UiState.Error -> {
                            Text(
                                text = (followers as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String, valueColor: Color = Color.White, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.48f))
    }
}

@Composable
private fun FollowerRow(
    follower: User,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = CodditCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(url = follower.avatarUrl, size = 36)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = follower.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = "@${follower.username}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun LinkedAccountItem(
    account: LinkedAccount,
    showUnlink: Boolean,
    onUnlink: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        color = CodditCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinkedAccountBadge(provider = account.provider.name, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.provider.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(text = account.displayData, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
            }
            if (account.verified) {
                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = SolvedGreen, modifier = Modifier.size(18.dp))
            }
            if (showUnlink) {
                TextButton(onClick = onUnlink) {
                    Text(text = "Unlink", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
                }
            }
        }
    }
}
