package com.coddit.app.data.remote.firestore

import com.coddit.app.domain.model.Reply
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReplyRemoteSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val postsCollection = firestore.collection("posts")

    suspend fun getReplies(postId: String): List<Reply> {
        val snapshot = try {
            postsCollection.document(postId).collection("replies")
                .orderBy("accepted", Query.Direction.DESCENDING)
                .orderBy("upvotes", Query.Direction.DESCENDING)
                .get().await()
        } catch (_: Exception) {
            // Fallback for missing composite index or mixed historical docs.
            postsCollection.document(postId).collection("replies")
                .get().await()
        }

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            val authorUid = data["authorUid"] as? String ?: return@mapNotNull null
            val body = data["body"] as? String ?: return@mapNotNull null
            Reply(
                replyId = doc.id,
                postId = postId,
                authorUid = authorUid,
                authorUsername = data["authorUsername"] as? String ?: "Anonymous",
                authorAvatarUrl = data["authorAvatarUrl"] as? String,
                authorLinkedAccounts = emptyList(),
                body = body,
                imageUrls = (data["imageUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                links = emptyList(),
                accepted = data["accepted"] as? Boolean ?: false,
                upvotes = (data["upvotes"] as? Long)?.toInt() ?: 0,
                createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
            )
        }.sortedWith(
            compareByDescending<Reply> { it.accepted }
                .thenByDescending { it.upvotes }
                .thenByDescending { it.createdAt }
        )
    }

    suspend fun saveReply(reply: Reply) {
        val data = hashMapOf(
            "authorUid" to reply.authorUid,
            "authorUsername" to reply.authorUsername,
            "authorAvatarUrl" to reply.authorAvatarUrl,
            "body" to reply.body,
            "imageUrls" to reply.imageUrls,
            "accepted" to reply.accepted,
            "upvotes" to reply.upvotes,
            "createdAt" to com.google.firebase.Timestamp(java.util.Date(reply.createdAt))
        )
        
        firestore.runTransaction { transaction ->
            val postRef = postsCollection.document(reply.postId)
            val replyRef = postRef.collection("replies").document(reply.replyId)
            
            transaction.set(replyRef, data)
            transaction.update(postRef, "replyCount", FieldValue.increment(1))
        }.await()
    }

    suspend fun voteReply(postId: String, replyId: String, voterUid: String): Boolean {
        return firestore.runTransaction { transaction ->
            val postRef = postsCollection.document(postId)
            val replyRef = postRef.collection("replies").document(replyId)
            val voteRef = replyRef.collection("votes").document(voterUid)
            val replySnapshot = transaction.get(replyRef)
            if (!replySnapshot.exists()) return@runTransaction false
            if (transaction.get(voteRef).exists()) return@runTransaction false

            val authorUid = replySnapshot.getString("authorUid").orEmpty()
            transaction.set(voteRef, mapOf("votedAt" to FieldValue.serverTimestamp()))
            transaction.update(replyRef, "upvotes", FieldValue.increment(1))
            if (authorUid.isNotBlank()) {
                transaction.update(firestore.collection("users").document(authorUid), "bytes", FieldValue.increment(1))
            }
            true
        }.await()
    }

    suspend fun updateReply(postId: String, replyId: String, body: String) {
        postsCollection.document(postId)
            .collection("replies")
            .document(replyId)
            .update(
                mapOf(
                    "body" to body,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            )
            .await()
    }

    suspend fun acceptReply(postId: String, replyId: String) {
        firestore.runTransaction { transaction ->
            val postRef = postsCollection.document(postId)
            val replyRef = postRef.collection("replies").document(replyId)
            
            // Mark this reply as accepted
            transaction.update(replyRef, "accepted", true)
            
            // Mark the post as solved
            transaction.update(postRef, "solved", true)
        }.await()
    }

    suspend fun deleteReply(postId: String, replyId: String) {
        firestore.runTransaction { transaction ->
            val postRef = postsCollection.document(postId)
            val replyRef = postRef.collection("replies").document(replyId)

            val postSnapshot = transaction.get(postRef)
            val existingCount = (postSnapshot.getLong("replyCount") ?: 0L).coerceAtLeast(0L)

            transaction.delete(replyRef)
            transaction.update(postRef, "replyCount", (existingCount - 1L).coerceAtLeast(0L))
        }.await()
    }
}
