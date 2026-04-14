package com.coddit.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val postId: String,
    val authorUid: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val authorLinkedAccounts: List<LinkedAccount> = emptyList(),
    val title: String,
    val body: String,
    val codeSnippet: String?,
    val imageUrls: List<String> = emptyList(),
    val tags: List<String>,
    val upvotes: Int,
    val viewCount: Int,
    val replyCount: Int,
    val solved: Boolean,
    val acceptedReplyId: String?,
    val createdAt: Long
)

@Serializable
data class Reply(
    val replyId: String,
    val postId: String,
    val authorUid: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val authorLinkedAccounts: List<LinkedAccount> = emptyList(),
    val body: String,
    val imageUrls: List<String> = emptyList(),
    val links: List<SafeLink>,
    val accepted: Boolean,
    val upvotes: Int,
    val createdAt: Long
)

@Serializable
data class User(
    val uid: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val bytes: Int,
    val postCount: Int,
    val followerCount: Int,
    val linkedAccounts: List<LinkedAccount>,
    val skills: List<String> = emptyList(),
    val createdAt: Long
)

@Serializable
data class LinkedAccount(
    val provider: LinkedAccountProvider,
    val handle: String,
    val profileUrl: String,
    val displayData: String,
    val verified: Boolean
)

enum class LinkedAccountProvider {
    GITHUB, LINKEDIN, GOOGLE, NPM, STACKOVERFLOW, DEVTO
}

@Serializable
data class SafeLink(
    val url: String,
    val displayUrl: String,
    val title: String?,
    val isVerified: Boolean,
    val isMalicious: Boolean,
    val isOnAllowlist: Boolean
)

@Serializable
data class BytesEvent(
    val eventId: String,
    val uid: String,
    val action: BytesAction,
    val delta: Int,
    val reason: String,
    val timestamp: Long
)

enum class BytesAction {
    POST_UPVOTED,
    REPLY_ACCEPTED,
    REPLY_UPVOTED,
    POST_100_VIEWS,
    LINK_VERIFIED
}
