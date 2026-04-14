package com.coddit.app.presentation.navigation

sealed class Screen(val route: String) {
    object Login         : Screen("login")
    object Username      : Screen("onboarding/username")
    object LinkAccounts  : Screen("onboarding/link-accounts")
    object TagPicker     : Screen("onboarding/tags")
    object Feed          : Screen("feed")
    object Search        : Screen("search")
    object CreatePost    : Screen("post/create")
    
    data class PostDetail(val postId: String) : Screen("post/$postId") {
        companion object { const val ROUTE = "post/{postId}" }
    }
    
    data class Profile(val uid: String) : Screen("profile/$uid") {
        companion object { const val ROUTE = "profile/{uid}" }
    }
    
    object MyProfile     : Screen("profile/me")
    object LinkAccount   : Screen("profile/link")
    object Notifications : Screen("notifications")
}
