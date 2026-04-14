package com.coddit.app.data.remote.firestore

import com.coddit.app.domain.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRemoteSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val postsCollection = firestore.collection("posts")
    private val usersCollection = firestore.collection("users")

    suspend fun getPostsByAuthor(authorUid: String): List<Post> {
        val snapshot = postsCollection
            .whereEqualTo("authorUid", authorUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            Post(
                postId = doc.id,
                authorUid = data["authorUid"] as String,
                authorUsername = data["authorUsername"] as? String ?: "Anonymous",
                authorAvatarUrl = data["authorAvatarUrl"] as String?,
                authorLinkedAccounts = emptyList(),
                title = data["title"] as String,
                body = data["body"] as String,
                codeSnippet = data["codeSnippet"] as String?,
                imageUrls = (data["imageUrls"] as? List<String>) ?: emptyList(),
                tags = (data["tags"] as? List<String>) ?: emptyList(),
                upvotes = (data["upvotes"] as? Long)?.toInt() ?: 0,
                viewCount = (data["viewCount"] as? Long)?.toInt() ?: 0,
                replyCount = (data["replyCount"] as? Long)?.toInt() ?: 0,
                solved = data["solved"] as? Boolean ?: false,
                acceptedReplyId = data["acceptedReplyId"] as String?,
                createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
            )
        }
    }

    suspend fun getFeed(tags: List<String>): List<Post> {
        val query = if (tags.isNotEmpty()) {
            postsCollection.whereArrayContainsAny("tags", tags)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
        } else {
            postsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
        }
        
        val snapshot = query.get().await()
        return snapshot.documents.map { doc ->
            val data = doc.data ?: return@map null
            Post(
                postId = doc.id,
                authorUid = data["authorUid"] as String,
                authorUsername = data["authorUsername"] as? String ?: "Anonymous",
                authorAvatarUrl = data["authorAvatarUrl"] as String?,
                authorLinkedAccounts = emptyList(), // Load separately if needed
                title = data["title"] as String,
                body = data["body"] as String,
                codeSnippet = data["codeSnippet"] as String?,
                imageUrls = (data["imageUrls"] as? List<String>) ?: emptyList(),
                tags = (data["tags"] as? List<String>) ?: emptyList(),
                upvotes = (data["upvotes"] as? Long)?.toInt() ?: 0,
                viewCount = (data["viewCount"] as? Long)?.toInt() ?: 0,
                replyCount = (data["replyCount"] as? Long)?.toInt() ?: 0,
                solved = data["solved"] as? Boolean ?: false,
                acceptedReplyId = data["acceptedReplyId"] as String?,
                createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
            )
        }.filterNotNull()
    }

    suspend fun savePost(post: Post) {
        val data = hashMapOf(
            "authorUid" to post.authorUid,
            "authorUsername" to post.authorUsername,
            "authorAvatarUrl" to post.authorAvatarUrl,
            "title" to post.title,
            "body" to post.body,
            "codeSnippet" to post.codeSnippet,
            "imageUrls" to post.imageUrls,
            "tags" to post.tags,
            "upvotes" to post.upvotes,
            "viewCount" to post.viewCount,
            "replyCount" to post.replyCount,
            "solved" to post.solved,
            "acceptedReplyId" to post.acceptedReplyId,
            "createdAt" to com.google.firebase.Timestamp(java.util.Date(post.createdAt))
        )
        firestore.runTransaction { transaction ->
            transaction.set(postsCollection.document(post.postId), data)
            transaction.update(usersCollection.document(post.authorUid), "postCount", FieldValue.increment(1))
        }.await()
    }

    suspend fun votePost(postId: String, delta: Int) {
        postsCollection.document(postId)
            .update("upvotes", FieldValue.increment(delta.toLong()))
            .await()
    }

    suspend fun incrementViewCount(postId: String) {
        postsCollection.document(postId)
            .update("viewCount", FieldValue.increment(1))
            .await()
    }

    suspend fun updatePost(postId: String, title: String, body: String, tags: List<String>, imageUrls: List<String>) {
        postsCollection.document(postId)
            .update(
                mapOf(
                    "title" to title,
                    "body" to body,
                    "tags" to tags,
                    "imageUrls" to imageUrls,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            )
            .await()
    }

    suspend fun deletePost(postId: String) {
        firestore.runTransaction { transaction ->
            val postRef = postsCollection.document(postId)
            val postSnapshot = transaction.get(postRef)
            val authorUid = postSnapshot.getString("authorUid").orEmpty()
            transaction.delete(postRef)
            if (authorUid.isNotBlank()) {
                val userRef = usersCollection.document(authorUid)
                val existingCount = (transaction.get(userRef).getLong("postCount") ?: 0L).coerceAtLeast(0L)
                transaction.update(userRef, "postCount", (existingCount - 1L).coerceAtLeast(0L))
            }
        }.await()
    }

    suspend fun updateAuthorAvatar(authorUid: String, newAvatarUrl: String?) {
        val postsQuery = postsCollection.whereEqualTo("authorUid", authorUid).get().await()
        val batch = firestore.batch()
        postsQuery.documents.forEach { doc ->
            batch.update(doc.reference, "authorAvatarUrl", newAvatarUrl)
        }
        if (postsQuery.documents.isNotEmpty()) {
            batch.commit().await()
        }
    }
}
