package com.coddit.app.presentation.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coddit.app.presentation.components.PostCard
import com.coddit.app.presentation.components.TagChip
import com.coddit.app.presentation.theme.CodditCard
import com.coddit.app.presentation.theme.CodditTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPostClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val posts by viewModel.posts.collectAsState()
    val trendingTags = listOf("All", "Kotlin", "Android", "React", "Python", "Jetpack")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color.Transparent)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = CodditCard,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(CodditTeal),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text("search posts, tags, people...", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trendingTags) { tag ->
                        TagChip(
                            tag = tag,
                            isSelected = searchQuery.equals(tag, ignoreCase = true),
                            onClick = { searchQuery = tag }
                        )
                    }
                }
            }
        }
    ) { padding ->
        val filtered = posts.filter {
            searchQuery.isBlank() ||
                it.title.contains(searchQuery, true) ||
            it.body.contains(searchQuery, true) ||
                it.tags.any { t -> t.contains(searchQuery, true) }
        }
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(46.dp), tint = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "no matching threads yet",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Try a stack, tool, or bug name like kotlin, firebase, auth, or compose.",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = "TRENDING TODAY",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                items(filtered, key = { it.postId }) { post ->
                    PostCard(
                        post = post,
                        onClick = { onPostClick(post.postId) },
                        onUpvote = {},
                        onShare = {}
                    )
                }
            }
        }
    }
}
