package com.coddit.app.data.repository

import android.net.Uri
import com.coddit.app.domain.model.SafeLink
import com.coddit.app.domain.repository.LinkSafetyRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkSafetyRepositoryImpl @Inject constructor() : LinkSafetyRepository {

    private val SAFE_DOMAINS = setOf(
        "github.com", "gist.github.com",
        "stackoverflow.com",
        "developer.android.com",
        "kotlinlang.org",
        "jetbrains.com",
        "docs.oracle.com",
        "developer.mozilla.org",
        "npmjs.com",
        "pub.dev",
        "medium.com",
        "dev.to",
        "docs.google.com",
        "firebase.google.com",
        "cloud.google.com",
        "reactjs.org", "react.dev",
        "nodejs.org",
        "python.org", "docs.python.org",
        "w3schools.com",
        "geeksforgeeks.org",
        "leetcode.com",
        "hackerrank.com",
        "youtube.com",
        "arxiv.org"
    )

    override suspend fun checkLinkSafety(url: String): SafeLink {
        val domain = try {
            Uri.parse(url).host?.removePrefix("www.") ?: ""
        } catch (e: Exception) {
            ""
        }

        if (SAFE_DOMAINS.any { domain == it || domain.endsWith(".$it") }) {
            return SafeLink(
                url = url,
                displayUrl = domain,
                isVerified = true,
                isMalicious = false,
                isOnAllowlist = true,
                title = null
            )
        }

        // TODO: Call Google Safe Browsing API v4
        // For now, assume unverified but not malicious unless explicitly blocked
        return SafeLink(
            url = url,
            displayUrl = domain,
            isVerified = false,
            isMalicious = false,
            isOnAllowlist = false,
            title = null
        )
    }
}
