package com.coddit.app.data.remote.algolia

import com.coddit.app.domain.model.Post
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class AlgoliaSearchRequest(
    val query: String,
    val filters: String? = null,
    val hitsPerPage: Int = 20
)

@Serializable
data class AlgoliaSearchResponse(
    val hits: List<AlgoliaPostHit>
)

@Serializable
data class AlgoliaPostHit(
    val objectID: String,
    val authorUid: String,
    val authorUsername: String,
    val title: String,
    val body: String,
    val tags: List<String>,
    val upvotes: Int,
    val viewCount: Int,
    val replyCount: Int,
    val solved: Boolean,
    val createdAt: Long
)

interface AlgoliaApi {
    @POST("1/indexes/{indexName}/query")
    suspend fun search(
        @Path("indexName") indexName: String,
        @Header("X-Algolia-Application-Id") appId: String,
        @Header("X-Algolia-API-Key") apiKey: String,
        @Body request: AlgoliaSearchRequest
    ): AlgoliaSearchResponse
}
