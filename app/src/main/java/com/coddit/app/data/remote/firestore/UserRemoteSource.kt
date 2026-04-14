package com.coddit.app.data.remote.firestore

import com.coddit.app.domain.model.User
import com.coddit.app.domain.model.LinkedAccount
import com.coddit.app.domain.model.LinkedAccountProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getUser(uid: String): User? {
        val doc = usersCollection.document(uid).get().await()
        return if (doc.exists()) {
            val data = doc.data ?: return null
            val linkedAccountsSnapshot = usersCollection.document(uid)
                .collection("linked_accounts")
                .get()
                .await()
            val linkedAccounts = linkedAccountsSnapshot.documents.mapNotNull { accountDoc ->
                val providerRaw = accountDoc.getString("provider") ?: return@mapNotNull null
                val provider = runCatching { LinkedAccountProvider.valueOf(providerRaw) }.getOrNull() ?: return@mapNotNull null
                LinkedAccount(
                    provider = provider,
                    handle = accountDoc.getString("handle") ?: "",
                    profileUrl = accountDoc.getString("profileUrl") ?: "",
                    displayData = accountDoc.getString("displayData") ?: "Connected",
                    verified = accountDoc.getBoolean("verified") ?: false
                )
            }

            User(
                uid = uid,
                username = data["username"] as String,
                displayName = data["displayName"] as String,
                avatarUrl = data["avatarUrl"] as String?,
                bytes = (data["bytes"] as? Long)?.toInt() ?: 0,
                postCount = (data["postCount"] as? Long)?.toInt() ?: 0,
                followerCount = (data["followerCount"] as? Long)?.toInt() ?: 0,
                linkedAccounts = linkedAccounts,
                skills = (data["skills"] as? List<String>) ?: emptyList(),
                createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
            )
        } else null
    }

    suspend fun saveUser(user: User) {
        val data = hashMapOf(
            "uid" to user.uid,
            "username" to user.username,
            "displayName" to user.displayName,
            "avatarUrl" to user.avatarUrl,
            "bytes" to user.bytes,
            "postCount" to user.postCount,
            "followerCount" to user.followerCount,
            "skills" to user.skills,
            "createdAt" to FieldValue.serverTimestamp()
        )
        usersCollection.document(user.uid).set(data).await()
    }

    suspend fun updateProfile(uid: String, avatarUrl: String? = null, skills: List<String>? = null) {
        val updates = mutableMapOf<String, Any>()
        avatarUrl?.let { updates["avatarUrl"] = it }
        skills?.let { updates["skills"] = it }
        if (updates.isNotEmpty()) {
            usersCollection.document(uid).update(updates).await()
        }
    }

    suspend fun updateBytes(uid: String, delta: Int) {
        usersCollection.document(uid).update("bytes", FieldValue.increment(delta.toLong())).await()
    }

    suspend fun isUsernameAvailable(username: String): Boolean {
        val query = usersCollection.whereEqualTo("username", username).limit(1).get().await()
        return query.isEmpty
    }

    suspend fun linkAccount(uid: String, account: LinkedAccount) {
        val data = hashMapOf(
            "provider" to account.provider.name,
            "handle" to account.handle,
            "profileUrl" to account.profileUrl,
            "displayData" to account.displayData,
            "verified" to account.verified,
            "linkedAt" to FieldValue.serverTimestamp()
        )
        usersCollection.document(uid)
            .collection("linked_accounts")
            .document(account.provider.name)
            .set(data).await()
    }

    suspend fun unlinkAccount(uid: String, provider: LinkedAccountProvider) {
        usersCollection.document(uid)
            .collection("linked_accounts")
            .document(provider.name)
            .delete()
            .await()
    }

    suspend fun followUser(followerUid: String, followedUid: String) {
        // Add to follower's following collection
        usersCollection.document(followerUid)
            .collection("following")
            .document(followedUid)
            .set(mapOf("followedAt" to System.currentTimeMillis()))
            .await()

        // Add to followed user's followers collection
        usersCollection.document(followedUid)
            .collection("followers")
            .document(followerUid)
            .set(mapOf("followedAt" to System.currentTimeMillis()))
            .await()

        // Increment follower count for followed user
        usersCollection.document(followedUid)
            .update("followerCount", FieldValue.increment(1))
            .await()
    }

    suspend fun unfollowUser(followerUid: String, followedUid: String) {
        // Remove from follower's following collection
        usersCollection.document(followerUid)
            .collection("following")
            .document(followedUid)
            .delete()
            .await()

        // Remove from followed user's followers collection
        usersCollection.document(followedUid)
            .collection("followers")
            .document(followerUid)
            .delete()
            .await()

        // Decrement follower count for followed user
        usersCollection.document(followedUid)
            .update("followerCount", FieldValue.increment(-1))
            .await()
    }

    suspend fun isFollowing(followerUid: String, followedUid: String): Boolean {
        val doc = usersCollection.document(followerUid)
            .collection("following")
            .document(followedUid)
            .get()
            .await()
        return doc.exists()
    }

    suspend fun getFollowers(uid: String): List<User> {
        val followersSnapshot = usersCollection.document(uid)
            .collection("followers")
            .orderBy("followedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        return followersSnapshot.documents.mapNotNull { followerDoc ->
            val followerUid = followerDoc.id
            val userDoc = usersCollection.document(followerUid).get().await()
            if (!userDoc.exists()) return@mapNotNull null

            val data = userDoc.data ?: return@mapNotNull null
            User(
                uid = followerUid,
                username = data["username"] as String,
                displayName = data["displayName"] as String,
                avatarUrl = data["avatarUrl"] as String?,
                bytes = (data["bytes"] as? Long)?.toInt() ?: 0,
                postCount = (data["postCount"] as? Long)?.toInt() ?: 0,
                followerCount = (data["followerCount"] as? Long)?.toInt() ?: 0,
                linkedAccounts = emptyList(),
                skills = (data["skills"] as? List<String>) ?: emptyList(),
                createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
            )
        }
    }
}
