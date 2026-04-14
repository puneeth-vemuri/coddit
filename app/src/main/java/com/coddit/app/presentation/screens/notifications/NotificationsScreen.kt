package com.coddit.app.presentation.screens.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coddit.app.presentation.theme.BytesPurple
import com.coddit.app.presentation.theme.CodditCard
import com.coddit.app.presentation.theme.CodditTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Simplified model for the UI redesign demo
data class NotificationItem(
    val id: String,
    val type: String, // "REPLY_ON_POST", "REPLY_ACCEPTED", "BYTES_AWARDED", "POST_UPVOTED"
    val message: String,
    val postId: String?,
    val isRead: Boolean,
    val createdAt: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onPostClick: (String) -> Unit
) {
    val notifications = remember { emptyList<NotificationItem>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "notifications",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = CodditCard,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { }) {
                        Text("mark all read", color = CodditTeal, fontWeight = FontWeight.Medium, fontSize = 10.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(42.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No notifications yet",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Replies, accepted solutions, and byte rewards will land here.",
                        color = Color.White.copy(alpha = 0.42f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 14.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItemRow(
                        notification = notification,
                        onClick = { notification.postId?.let { onPostClick(it) } }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItemRow(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val isReward = notification.type == "BYTES_AWARDED" || notification.type == "REPLY_ACCEPTED"
    val accent = when (notification.type) {
        "REPLY_ON_POST" -> CodditTeal
        "REPLY_ACCEPTED" -> CodditTeal
        "BYTES_AWARDED" -> CodditTeal
        "POST_UPVOTED" -> BytesPurple
        else -> Color.White
    }
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(0.dp),
        color = if (isReward) CodditTeal.copy(alpha = 0.06f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when(notification.type) {
                            "REPLY_ON_POST" -> Icons.AutoMirrored.Filled.Chat
                            "REPLY_ACCEPTED" -> Icons.Default.Check
                            "BYTES_AWARDED" -> Icons.Default.ThumbUp
                            else -> Icons.Default.Star
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = accent
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.message,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color.White,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTimestamp(notification.createdAt),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(CodditTeal, CircleShape)
                )
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 14.dp),
        color = Color.White.copy(alpha = 0.05f)
    )
}

private fun formatTimestamp(timeMillis: Long): String {
    val diff = System.currentTimeMillis() - timeMillis
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000} minutes ago"
        diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timeMillis))
    }
}
