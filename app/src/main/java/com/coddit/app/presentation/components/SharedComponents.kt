package com.coddit.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.coddit.app.R

import com.coddit.app.presentation.theme.GithubPrimary
import com.coddit.app.presentation.theme.LinkedInBlue
import com.coddit.app.presentation.theme.CodditTeal
import com.coddit.app.presentation.theme.BytesPurple
import com.coddit.app.presentation.theme.SolvedGreen

@Composable
fun UserAvatar(
    url: String?,
    size: Int = 40,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    AsyncImage(
        model = url,
        contentDescription = "User Avatar",
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
        error = painterResource(id = R.drawable.ic_launcher_foreground)
    )
}

@Composable
fun TagChip(
    tag: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        selected = isSelected,
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) CodditTeal.copy(alpha = 0.1f) else Color.Transparent,
        border = BorderStroke(
            1.dp, 
            if (isSelected) CodditTeal else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = tag.lowercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) CodditTeal else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BytesPill(
    bytes: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BytesPurple.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "+$bytes bytes",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = BytesPurple
            )
        }
    }
}

@Composable
fun ImageCarousel(
    images: List<String>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images) { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = "Post Image",
                modifier = Modifier
                    .size(width = 300.dp, height = 200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0D1117), // GitHub-style deep dark
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(16.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = CodditTeal.copy(alpha = 0.9f),
            lineHeight = 18.sp
        )
    }
}

@Composable
fun LinkedAccountBadge(
    provider: String,
    modifier: Modifier = Modifier
) {
    val bgColor = when(provider.uppercase()) {
        "GITHUB" -> GithubPrimary
        "LINKEDIN" -> LinkedInBlue
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.height(18.dp).padding(horizontal = 1.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = provider.take(2).uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}
