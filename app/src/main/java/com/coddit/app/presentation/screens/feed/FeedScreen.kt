package com.coddit.app.presentation.screens.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.coddit.app.presentation.theme.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coddit.app.presentation.components.PostCard
import com.coddit.app.presentation.components.TagChip
import com.coddit.app.presentation.util.UiState
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onCreatePost: () -> Unit,
    onPostClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_uid"

    val allTags = listOf("all", "android", "kotlin", "react", "python", "firebase", "compose")

    Scaffold(
        topBar = {
            Surface(
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "<coddit/>",
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier
                                .size(36.dp)
                                .background(CodditCard, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.75f))
                        }
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier
                                .size(36.dp)
                                .background(CodditCard, RoundedCornerShape(12.dp))
                        ) {
                            BadgedBox(badge = { Badge { Text("1") } }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White.copy(alpha = 0.75f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        onClick = onSearchClick,
                        shape = RoundedCornerShape(14.dp),
                        color = CodditCard,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.36f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "search posts, tags, people...",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePost,
                containerColor = CodditTeal,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CodditSurface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
                    label = { Text("feed", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CodditTeal,
                        selectedTextColor = CodditTeal,
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f),
                        indicatorColor = CodditTeal.copy(alpha = 0.16f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSearchClick,
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("search", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CodditTeal,
                        selectedTextColor = CodditTeal,
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f),
                        indicatorColor = CodditTeal.copy(alpha = 0.16f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("profile", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CodditTeal,
                        selectedTextColor = CodditTeal,
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f),
                        indicatorColor = CodditTeal.copy(alpha = 0.16f)
                    )
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allTags) { tag ->
                    TagChip(
                        tag = tag,
                        isSelected = tag in selectedTags,
                        onClick = { viewModel.onTagSelected(tag) }
                    )
                }
            }

            Text(
                text = "TRENDING TODAY",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = Color.White.copy(alpha = 0.55f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CodditTeal)
                    }
                }
                is UiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.loadFeed() },
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            items(state.data, key = { it.postId }) { post ->
                                PostCard(
                                    post = post,
                                    onClick = { onPostClick(post.postId) },
                                    onUpvote = { viewModel.onVotePost(post.postId, currentUid) },
                                    onShare = { /* Share handled in VM */ }
                                )
                            }
                        }
                    }
                }
                is UiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text(
                                "No threads in this filter yet",
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Start the conversation with a real coding problem and let other builders reply with solutions.",
                                color = Color.White.copy(alpha = 0.48f),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Create the first post",
                                color = CodditTeal,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(onClick = onCreatePost)
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Exception in feed loop: ${state.message}", color = MaterialTheme.colorScheme.error, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
