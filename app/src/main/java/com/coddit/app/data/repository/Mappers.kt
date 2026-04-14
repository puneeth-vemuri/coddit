package com.coddit.app.data.repository

import com.coddit.app.data.local.db.entity.PostEntity
import com.coddit.app.data.local.db.entity.ReplyEntity
import com.coddit.app.data.local.db.entity.UserEntity
import com.coddit.app.domain.model.Post
import com.coddit.app.domain.model.Reply
import com.coddit.app.domain.model.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Mappers {
    private val json = Json { ignoreUnknownKeys = true }

    fun PostEntity.toDomain(): Post = Post(
        postId = postId,
        authorUid = authorUid,
        authorUsername = authorUsername,
        authorAvatarUrl = authorAvatarUrl,
        authorLinkedAccounts = emptyList(),
        title = title,
        body = body,
        codeSnippet = codeSnippet,
        imageUrls = try { json.decodeFromString(imageUrlsJson) } catch (e: Exception) { emptyList() },
        tags = try { json.decodeFromString(tagsJson) } catch (e: Exception) { tagsJson.split(",").filter { it.isNotEmpty() } },
        upvotes = upvotes,
        viewCount = viewCount,
        replyCount = replyCount,
        solved = solved,
        acceptedReplyId = acceptedReplyId,
        createdAt = createdAt
    )

    fun Post.toEntity(): PostEntity = PostEntity(
        postId = postId,
        authorUid = authorUid,
        authorUsername = authorUsername,
        authorAvatarUrl = authorAvatarUrl,
        title = title,
        body = body,
        codeSnippet = codeSnippet,
        imageUrlsJson = json.encodeToString(imageUrls),
        tagsJson = json.encodeToString(tags),
        upvotes = upvotes,
        viewCount = viewCount,
        replyCount = replyCount,
        solved = solved,
        acceptedReplyId = acceptedReplyId,
        createdAt = createdAt
    )

    fun ReplyEntity.toDomain(): Reply = Reply(
        replyId = replyId,
        postId = postId,
        authorUid = authorUid,
        authorUsername = authorUsername,
        authorAvatarUrl = authorAvatarUrl,
        authorLinkedAccounts = emptyList(),
        body = body,
        imageUrls = try { json.decodeFromString(imageUrlsJson) } catch (e: Exception) { emptyList() },
        links = emptyList(),
        accepted = accepted,
        upvotes = upvotes,
        createdAt = createdAt
    )

    fun Reply.toEntity(): ReplyEntity = ReplyEntity(
        replyId = replyId,
        postId = postId,
        authorUid = authorUid,
        authorUsername = authorUsername,
        authorAvatarUrl = authorAvatarUrl,
        body = body,
        imageUrlsJson = json.encodeToString(imageUrls),
        linksJson = "", // Placeholder
        accepted = accepted,
        upvotes = upvotes,
        createdAt = createdAt
    )

    fun UserEntity.toDomain(): User = User(
        uid = uid,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bytes = bytes,
        postCount = postCount,
        followerCount = followerCount,
        linkedAccounts = try { json.decodeFromString(linkedAccountsJson) } catch (e: Exception) { emptyList() },
        skills = try { json.decodeFromString(skillsJson) } catch (e: Exception) { emptyList() },
        createdAt = 0
    )

    fun User.toEntity(): UserEntity = UserEntity(
        uid = uid,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bytes = bytes,
        postCount = postCount,
        followerCount = followerCount,
        linkedAccountsJson = json.encodeToString(linkedAccounts),
        skillsJson = json.encodeToString(skills)
    )
}
