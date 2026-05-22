package com.samiuysal.fediversehub.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.samiuysal.fediversehub.core.designsystem.component.AppBottomBar
import com.samiuysal.fediversehub.core.designsystem.component.AppScaffold
import com.samiuysal.fediversehub.feature.auth.MastodonAuthRoute
import com.samiuysal.fediversehub.feature.home.HomeRoute

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

    AppScaffold(
        bottomBar = {
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
        },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.HOME,
        ) {
            composable(AppDestination.HOME) {
                HomeRoute(contentPadding = contentPadding)
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
                PlaceholderRoute(
                    title = "Notifications",
                    message = "Mentions, replies, boosts, votes and follows will share one inbox shell.",
                    contentPadding = contentPadding,
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
