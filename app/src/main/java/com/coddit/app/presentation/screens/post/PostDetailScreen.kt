package com.coddit.app.presentation.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Share
import com.coddit.app.presentation.components.TagChip
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil3.compose.AsyncImage
import com.coddit.app.presentation.components.ReplyCard
import com.coddit.app.presentation.components.UserAvatar
import com.coddit.app.presentation.components.ImageCarousel
import com.coddit.app.presentation.components.LinkedAccountBadge
import com.coddit.app.presentation.util.UiState
import com.google.firebase.auth.FirebaseAuth

import androidx.compose.foundation.BorderStroke
import com.coddit.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    viewModel: PostDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val postState by viewModel.postState.collectAsState()
    val repliesState by viewModel.repliesState.collectAsState()
    val replyImages by viewModel.replyImages.collectAsState()
    val postDeleted by viewModel.postDeleted.collectAsState()
    var replyText by remember { mutableStateOf("") }
    var postMenuExpanded by remember { mutableStateOf(false) }
    var editingPost by remember { mutableStateOf(false) }
    var editingReply by remember { mutableStateOf<com.coddit.app.domain.model.Reply?>(null) }
    var editedPostTitle by remember { mutableStateOf("") }
    var editedPostBody by remember { mutableStateOf("") }
    var editedPostTagsInput by remember { mutableStateOf("") }
    var editedPostImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var editedReplyBody by remember { mutableStateOf("") }
    val authUser = FirebaseAuth.getInstance().currentUser
    val currentUid = authUser?.uid ?: "unknown_uid"
    val currentUsername = authUser?.displayName?.takeIf { it.isNotBlank() } ?: "anonymous"
    val currentAvatarUrl = authUser?.photoUrl?.toString()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris ->
            uris.forEach { uri ->
                viewModel.addReplyImage(uri.toString())
            }
        }
    )

    val editPostPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris ->
            val newUris = uris.map { it.toString() }
            editedPostImages = (editedPostImages + newUris).distinct().take(10)
        }
    )

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    LaunchedEffect(postDeleted) {
        if (postDeleted) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "post detail",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
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
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.82f))
                            }
                        }
                    }
                },
                actions = {
                    val postData = (postState as? UiState.Success)?.data
                    val isPostOwner = postData?.authorUid == currentUid

                    if (isPostOwner) {
                        Box {
                            IconButton(onClick = { postMenuExpanded = true }) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = CodditCard,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.MoreVert, "Post actions", modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.82f))
                                    }
                                }
                            }

                            DropdownMenu(expanded = postMenuExpanded, onDismissRequest = { postMenuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        postMenuExpanded = false
                                        postData?.let {
                                            editedPostTitle = it.title
                                            editedPostBody = it.body
                                            editedPostTagsInput = it.tags.joinToString(", ")
                                            editedPostImages = it.imageUrls
                                            editingPost = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        postMenuExpanded = false
                                        postData?.let { viewModel.onDeletePost(it.postId) }
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = { /* Share */ }) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = CodditCard,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Share, "Share", modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.82f))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                color = CodditSurface,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                    if (replyImages.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(replyImages) { uri ->
                                Box(modifier = Modifier.size(60.dp)) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeReplyImage(uri) },
                                        modifier = Modifier.size(20.dp).align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            tonalElevation = 2.dp,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                                BasicTextField(
                                    value = replyText,
                                    onValueChange = { replyText = it },
                                    modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(CodditTeal),
                                    decorationBox = { innerTextField ->
                                        if (replyText.isEmpty()) Text("write a reply...", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                                        innerTextField()
                                    }
                                )
                                IconButton(
                                    onClick = { 
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Image", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        FloatingActionButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    viewModel.onPostReply(postId, currentUid, currentUsername, currentAvatarUrl, replyText)
                                    replyText = ""
                                }
                            },
                            containerColor = CodditTeal,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Send, "Send", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (val post = postState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CodditTeal)
                }
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize()
                ) {
                    item {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(url = post.data.authorAvatarUrl, size = 44)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = post.data.authorUsername, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        post.data.authorLinkedAccounts.forEach { account ->
                                            LinkedAccountBadge(provider = account.provider.name)
                                        }
                                    }
                                    Text(text = "${post.data.viewCount} views", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = post.data.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 30.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = post.data.body, fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f), lineHeight = 24.sp)

                            if (post.data.imageUrls.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                ImageCarousel(images = post.data.imageUrls)
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                post.data.tags.forEach { tag ->
                                    TagChip(tag = tag)
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, null, modifier = Modifier.size(18.dp), tint = CodditTeal)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${post.data.upvotes}", fontWeight = FontWeight.Bold, color = CodditTeal)
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(Icons.AutoMirrored.Outlined.Chat, null, modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.width(8.dp))
                                val replyCount = (repliesState as? UiState.Success)?.data?.size ?: post.data.replyCount
                                Text("${replyCount} replies", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Medium)
                                
                                Spacer(modifier = Modifier.weight(1f))
                                if (post.data.solved) {
                                    Surface(
                                        color = SolvedGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, SolvedGreen.copy(alpha = 0.2f))
                                    ) {
                                        Text("✓ solved", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = SolvedGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.03f)))
                    }

                    when (val replies = repliesState) {
                        is UiState.Success -> {
                            // Sort so accepted reply is first
                            val sortedReplies = replies.data.sortedByDescending { it.accepted }
                            items(sortedReplies) { reply ->
                                val canEditReply = reply.authorUid == currentUid
                                val canDeleteReply = canEditReply || post.data.authorUid == currentUid

                                ReplyCard(
                                    reply = reply,
                                    onUpvote = { viewModel.onVoteReply(postId, reply.replyId, currentUid) },
                                    onAccept = { viewModel.onAcceptReply(postId, reply.replyId) },
                                    canAccept = !post.data.solved || reply.accepted,
                                    canEdit = canEditReply,
                                    canDelete = canDeleteReply,
                                    onEdit = {
                                        editingReply = reply
                                        editedReplyBody = reply.body
                                    },
                                    onDelete = {
                                        viewModel.onDeleteReply(postId, reply.replyId)
                                    }
                                )
                            }
                        }
                        is UiState.Empty -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("no replies yet. loop in?", color = Color.White.copy(alpha = 0.3f), fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                        else -> {}
                    }
                    
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error in detail stream: ${post.message}", color = MaterialTheme.colorScheme.error, fontFamily = FontFamily.Monospace)
                }
            }
            else -> {}
        }
    }

    if (editingPost) {
        AlertDialog(
            onDismissRequest = { editingPost = false },
            title = { Text("Edit post") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editedPostTitle,
                        onValueChange = { editedPostTitle = it },
                        label = { Text("Title") }
                    )
                    OutlinedTextField(
                        value = editedPostBody,
                        onValueChange = { editedPostBody = it },
                        label = { Text("Body") }
                    )
                    OutlinedTextField(
                        value = editedPostTagsInput,
                        onValueChange = { editedPostTagsInput = it },
                        label = { Text("Tags (comma separated)") }
                    )
                    if (editedPostImages.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(editedPostImages) { image ->
                                Box(modifier = Modifier.size(64.dp)) {
                                    AsyncImage(
                                        model = image,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { editedPostImages = editedPostImages - image },
                                        modifier = Modifier.align(Alignment.TopEnd).size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove image",
                                            tint = Color.White,
                                            modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    TextButton(onClick = {
                        editPostPhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Text("Add images")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsedTags = editedPostTagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    viewModel.onEditPost(postId, editedPostTitle, editedPostBody, parsedTags, editedPostImages)
                    editingPost = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingPost = false }) { Text("Cancel") }
            }
        )
    }

    if (editingReply != null) {
        AlertDialog(
            onDismissRequest = { editingReply = null },
            title = { Text("Edit reply") },
            text = {
                OutlinedTextField(
                    value = editedReplyBody,
                    onValueChange = { editedReplyBody = it },
                    label = { Text("Reply") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val reply = editingReply
                    if (reply != null) {
                        viewModel.onEditReply(postId, reply.replyId, editedReplyBody)
                    }
                    editingReply = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingReply = null }) { Text("Cancel") }
            }
        )
    }
}
