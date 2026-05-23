package com.samiuysal.fediversehub.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samiuysal.fediversehub.core.designsystem.component.AppBottomBar
import com.samiuysal.fediversehub.core.designsystem.component.AppScaffold
import com.samiuysal.fediversehub.feature.auth.MastodonAuthRoute
import com.samiuysal.fediversehub.feature.home.HomeRoute
import com.samiuysal.fediversehub.feature.mastodon.detail.MastodonPostDetailRoute
import com.samiuysal.fediversehub.feature.mastodon.media.FullScreenMediaViewer
import com.samiuysal.fediversehub.feature.mastodon.notifications.MastodonNotificationsRoute

@Composable
fun FediverseHubApp(
    oauthCallbackUri: Uri? = null,
    onOAuthCallbackConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedRoute = backStackEntry?.destination?.route ?: AppDestination.HOME

    LaunchedEffect(oauthCallbackUri) {
        if (oauthCallbackUri != null) {
            navController.navigate(AppDestination.PROFILE) {
                launchSingleTop = true
            }
        }
    }

    val showBottomBar = AppDestination.bottomNavItems.any { it.route == selectedRoute }

    AppScaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    items = AppDestination.bottomNavItems,
                    selectedRoute = selectedRoute,
                    onItemSelected = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(AppDestination.HOME) {
                                saveState = true
                            }
                        }
                    },
                )
            }
        },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.HOME,
        ) {
            composable(AppDestination.HOME) {
                HomeRoute(
                    contentPadding = contentPadding,
                    onMastodonPostSelected = { postId ->
                        navController.navigate(AppDestination.mastodonPostDetail(Uri.encode(postId)))
                    },
                    onMastodonMediaSelected = { urls, altFlags, index ->
                        navController.navigate(AppDestination.mastodonMediaViewer(urls, altFlags, index))
                    },
                    onMastodonUnauthorized = {
                        navController.navigate(AppDestination.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(AppDestination.SEARCH) {
                PlaceholderRoute(
                    title = "Search",
                    message = "Unified search will query people, communities, posts and tags by selected platform.",
                    contentPadding = contentPadding,
                )
            }
            composable(AppDestination.CREATE) {
                PlaceholderRoute(
                    title = "Create",
                    message = "Composer will adapt to toot, Lemmy post/comment, or Pixelfed media publishing.",
                    contentPadding = contentPadding,
                )
            }
            composable(AppDestination.NOTIFICATIONS) {
                MastodonNotificationsRoute(
                    contentPadding = contentPadding,
                    onPostSelected = { postId ->
                        navController.navigate(AppDestination.mastodonPostDetail(Uri.encode(postId)))
                    },
                    onProfileSelected = {
                        navController.navigate(AppDestination.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(AppDestination.PROFILE) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.padding(contentPadding),
                ) {
                    MastodonAuthRoute(
                        oauthCallbackUri = oauthCallbackUri,
                        onOAuthCallbackConsumed = onOAuthCallbackConsumed,
                    )
                }
            }
            composable(AppDestination.AUTH_MASTODON) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.padding(contentPadding),
                ) {
                    MastodonAuthRoute(
                        oauthCallbackUri = oauthCallbackUri,
                        onOAuthCallbackConsumed = onOAuthCallbackConsumed,
                    )
                }
            }
            composable(
                route = AppDestination.MASTODON_POST_DETAIL,
                arguments = listOf(
                    navArgument(AppDestination.POST_ID_ARGUMENT) {
                        type = NavType.StringType
                    },
                ),
            ) {
                MastodonPostDetailRoute(
                    contentPadding = contentPadding,
                    onBack = navController::navigateUp,
                    onMediaSelected = { urls, altFlags, index ->
                        navController.navigate(AppDestination.mastodonMediaViewer(urls, altFlags, index))
                    },
                    onUnauthorized = {
                        navController.navigate(AppDestination.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                route = AppDestination.MASTODON_MEDIA_VIEWER,
                arguments = listOf(
                    navArgument(AppDestination.MEDIA_URLS_ARGUMENT) {
                        type = NavType.StringType
                    },
                    navArgument(AppDestination.MEDIA_ALTS_ARGUMENT) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(AppDestination.MEDIA_INDEX_ARGUMENT) {
                        type = NavType.IntType
                        defaultValue = 0
                    },
                ),
            ) { entry ->
                val urls = entry.arguments
                    ?.getString(AppDestination.MEDIA_URLS_ARGUMENT)
                    ?.let(Uri::decode)
                    ?.split("|")
                    ?.filter(String::isNotBlank)
                    .orEmpty()
                val altFlags = entry.arguments
                    ?.getString(AppDestination.MEDIA_ALTS_ARGUMENT)
                    ?.split(",")
                    ?.map { it == "1" }
                    .orEmpty()
                val index = entry.arguments?.getInt(AppDestination.MEDIA_INDEX_ARGUMENT) ?: 0
                FullScreenMediaViewer(
                    urls = urls,
                    altFlags = altFlags,
                    initialIndex = index,
                    onBack = navController::navigateUp,
                )
            }
        }
    }
}

@Composable
private fun PlaceholderRoute(
    title: String,
    message: String,
    contentPadding: PaddingValues,
) {
    com.samiuysal.fediversehub.core.designsystem.component.EmptyState(
        title = title,
        message = message,
        modifier = Modifier.padding(contentPadding),
    )
}
