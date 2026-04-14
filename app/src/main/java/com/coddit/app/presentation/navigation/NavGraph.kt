package com.coddit.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.coddit.app.presentation.screens.auth.LoginScreen
import com.coddit.app.presentation.screens.onboarding.LinkAccountsScreen
import com.coddit.app.presentation.screens.onboarding.UsernameScreen
import com.coddit.app.presentation.screens.onboarding.TagPickerScreen
import com.coddit.app.presentation.screens.feed.FeedScreen
import com.coddit.app.presentation.screens.post.PostDetailScreen
import com.coddit.app.presentation.screens.post.CreatePostScreen
import com.coddit.app.presentation.screens.profile.ProfileScreen
import com.coddit.app.presentation.screens.search.SearchScreen
import com.coddit.app.presentation.screens.notifications.NotificationsScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { completed ->
                    val destination = if (completed) Screen.Feed.route else Screen.Username.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Username.route) {
            UsernameScreen(
                onNext = { navController.navigate(Screen.LinkAccounts.route) }
            )
        }

        composable(Screen.LinkAccounts.route) {
            LinkAccountsScreen(
                onContinue = { navController.navigate(Screen.TagPicker.route) },
                onSkip = { navController.navigate(Screen.TagPicker.route) }
            )
        }

        composable(Screen.LinkAccount.route) {
            LinkAccountsScreen(
                onContinue = { navController.popBackStack() },
                onSkip = { navController.popBackStack() }
            )
        }
        
        composable(Screen.TagPicker.route) {
            TagPickerScreen(
                onComplete = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Username.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Feed.route) {
            FeedScreen(
                onCreatePost = { navController.navigate(Screen.CreatePost.route) },
                onPostClick = { postId -> navController.navigate(Screen.PostDetail(postId).route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onProfileClick = { navController.navigate(Screen.MyProfile.route) }
            )
        }
        
        composable(Screen.PostDetail.ROUTE) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            PostDetailScreen(
                postId = postId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Profile.ROUTE) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: return@composable
            ProfileScreen(
                uid = uid,
                onBack = { navController.popBackStack() },
                onLinkAccount = { navController.navigate(Screen.LinkAccount.route) },
                onPostClick = { postId -> navController.navigate(Screen.PostDetail(postId).route) }
            )
        }

        composable(Screen.MyProfile.route) {
            val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: "demo_user"
            ProfileScreen(
                uid = myUid,
                onBack = { navController.popBackStack() },
                onLinkAccount = { navController.navigate(Screen.LinkAccount.route) },
                onPostClick = { postId -> navController.navigate(Screen.PostDetail(postId).route) }
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onPostClick = { postId -> navController.navigate(Screen.PostDetail(postId).route) }
            )
        }
        
        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onBack = { navController.popBackStack() },
                onPostSuccess = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onPostClick = { postId -> navController.navigate(Screen.PostDetail(postId).route) }
            )
        }
    }
}
