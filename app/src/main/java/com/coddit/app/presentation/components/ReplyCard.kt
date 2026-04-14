package com.coddit.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.coddit.app.domain.model.Reply
import com.coddit.app.presentation.theme.CodditTeal
import com.coddit.app.presentation.theme.SolvedGreen

@Composable
fun ReplyCard(
    reply: Reply,
    onUpvote: () -> Unit,
    onAccept: () -> Unit,
    canAccept: Boolean = false,
    canEdit: Boolean = false,
    canDelete: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val isAccepted = reply.accepted
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = if (isAccepted) 1.5.dp else 1.dp,
            color = if (isAccepted) SolvedGreen.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(if (isAccepted) SolvedGreen.copy(alpha = 0.04f) else Color.Transparent)
        ) {
            if (isAccepted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Surface(
                        color = SolvedGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Accepted +10 bytes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = SolvedGreen
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(url = reply.authorAvatarUrl, size = 32)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = reply.authorUsername,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                reply.authorLinkedAccounts.forEach { account ->
                    LinkedAccountBadge(provider = account.provider.name)
                }

                if (canEdit || canDelete) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Reply actions",
                                tint = Color.White.copy(alpha = 0.72f)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            if (canEdit) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        menuExpanded = false
                                        onEdit()
                                    }
                                )
                            }
                            if (canDelete) {
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = reply.body,
                fontSize = 15.sp,
                color = if (isAccepted) Color.White else Color.White.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )

            if (reply.imageUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.size(12.dp))
                ImageCarousel(images = reply.imageUrls)
            }

            if (reply.links.isNotEmpty()) {
                Spacer(modifier = Modifier.size(12.dp))
                reply.links.forEach { link ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = CodditTeal.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        link.url.take(1).uppercase(),
                                        color = CodditTeal,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    link.displayUrl,
                                    fontSize = 13.sp,
                                    color = CodditTeal,
                                    fontWeight = FontWeight.Medium
                                )
                                link.title?.let {
                                    Text(it, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                                }
                            }
                            Text("Safe", color = SolvedGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onUpvote, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = "Bytes",
                            modifier = Modifier.size(16.dp),
                            tint = CodditTeal
                        )
                    }
                    Text(text = "${reply.upvotes}", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                }

                if (canAccept && !isAccepted) {
                    TextButton(onClick = onAccept) {
                        Text("Accept Solution", color = SolvedGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
