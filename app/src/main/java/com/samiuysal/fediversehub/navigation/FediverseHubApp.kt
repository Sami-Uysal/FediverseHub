package com.samiuysal.fediversehub.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samiuysal.fediversehub.core.designsystem.component.AppBottomBar
import com.samiuysal.fediversehub.core.designsystem.component.AppScaffold
import com.samiuysal.fediversehub.core.designsystem.theme.PlatformColors
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.MastodonAuthRoute
import com.samiuysal.fediversehub.feature.explore.ExploreRoute
import com.samiuysal.fediversehub.feature.home.HomeRoute
import com.samiuysal.fediversehub.feature.lemmy.detail.LemmyPostDetailRoute
import com.samiuysal.fediversehub.feature.mastodon.detail.MastodonPostDetailRoute
import com.samiuysal.fediversehub.feature.mastodon.media.FullScreenMediaViewer
import com.samiuysal.fediversehub.feature.notifications.PlatformNotificationsRoute
import com.samiuysal.fediversehub.feature.pixelfed.detail.PixelfedPostDetailRoute
import com.samiuysal.fediversehub.feature.profile.PlatformProfileRoute
import com.samiuysal.fediversehub.feature.search.PlatformSearchRoute
import com.samiuysal.fediversehub.feature.settings.SettingsRoute

@Composable
fun FediverseHubApp(
    oauthCallbackUri: Uri? = null,
    onOAuthCallbackConsumed: () -> Unit = {},
    appStateViewModel: AppStateViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val appState by appStateViewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedRoute = backStackEntry?.destination?.route ?: AppDestination.HOME

    LaunchedEffect(oauthCallbackUri) {
        if (oauthCallbackUri != null) {
            when (oauthCallbackUri.path) {
                "/mastodon" -> appStateViewModel.selectPlatform(PlatformType.MASTODON)
                "/pixelfed" -> appStateViewModel.selectPlatform(PlatformType.PIXELFED)
            }
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
                    accentColor = appState.selectedPlatform.accentColor,
                    profileAvatarUrl = appState.selectedAccount?.avatarUrl,
                    profileName = appState.selectedAccount?.displayName
                        ?: appState.selectedAccount?.username
                        ?: "Profile",
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
                    selectedPlatform = appState.selectedPlatform,
                    selectedAccount = appState.selectedAccount,
                    onPlatformSelected = appStateViewModel::selectPlatform,
                    onMastodonPostSelected = { postId ->
                        navController.navigate(AppDestination.mastodonPostDetail(Uri.encode(postId)))
                    },
                    onPixelfedPostSelected = { postId ->
                        navController.navigate(AppDestination.pixelfedPostDetail(Uri.encode(postId)))
                    },
                    onLemmyPostSelected = { postId ->
                        navController.navigate(AppDestination.lemmyPostDetail(Uri.encode(postId)))
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
                PlatformSearchRoute(
                    selectedPlatform = appState.selectedPlatform,
                    selectedAccount = appState.selectedAccount,
                    contentPadding = contentPadding,
                    onPlatformSelected = appStateViewModel::selectPlatform,
                    onPostSelected = { postId ->
                        navController.navigate(AppDestination.mastodonPostDetail(Uri.encode(postId)))
                    },
                    onAccountSelected = { accountId ->
                        navController.navigate(AppDestination.searchAccountPlaceholder(accountId))
                    },
                    onHashtagSelected = { hashtag ->
                        navController.navigate(AppDestination.searchHashtagPlaceholder(hashtag))
                    },
                )
            }
            composable(AppDestination.EXPLORE) {
                ExploreRoute(
                    selectedPlatform = appState.selectedPlatform,
                    selectedAccount = appState.selectedAccount,
                    contentPadding = contentPadding,
                    onPostSelected = { postId ->
                        navController.navigate(AppDestination.mastodonPostDetail(Uri.encode(postId)))
                    },
                    onPixelfedPostSelected = { postId ->
                        navController.navigate(AppDestination.pixelfedPostDetail(Uri.encode(postId)))
                    },
                    onLemmyPostSelected = { postId ->
                        navController.navigate(AppDestination.lemmyPostDetail(Uri.encode(postId)))
                    },
                    onHashtagSelected = { hashtag ->
                        navController.navigate(AppDestination.searchHashtagPlaceholder(hashtag))
                    },
                    onMediaSelected = { urls, altFlags, index ->
                        navController.navigate(AppDestination.mastodonMediaViewer(urls, altFlags, index))
                    },
                )
            }
            composable(AppDestination.NOTIFICATIONS) {
                PlatformNotificationsRoute(
                    selectedPlatform = appState.selectedPlatform,
                    selectedAccount = appState.selectedAccount,
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
                PlatformProfileRoute(
                    selectedPlatform = appState.selectedPlatform,
                    platformAccounts = appState.platformAccounts,
                    selectedAccount = appState.selectedAccount,
                    contentPadding = contentPadding,
                    oauthCallbackUri = oauthCallbackUri,
                    onOAuthCallbackConsumed = onOAuthCallbackConsumed,
                    onPostSelected = { postId ->
                        navController.navigate(AppDestination.mastodonPostDetail(Uri.encode(postId)))
                    },
                    onPixelfedPostSelected = { postId ->
                        navController.navigate(AppDestination.pixelfedPostDetail(Uri.encode(postId)))
                    },
                    onMediaSelected = { urls, altFlags, index ->
                        navController.navigate(AppDestination.mastodonMediaViewer(urls, altFlags, index))
                    },
                    onPlatformSelected = appStateViewModel::selectPlatform,
                    onAccountSelected = appStateViewModel::selectAccount,
                    onSettingsClick = {
                        navController.navigate(AppDestination.SETTINGS)
                    },
                )
            }
            composable(AppDestination.SETTINGS) {
                SettingsRoute(
                    selectedPlatform = appState.selectedPlatform,
                    platformAccounts = appState.platformAccounts,
                    selectedAccount = appState.selectedAccount,
                    contentPadding = contentPadding,
                    onAccountSelected = appStateViewModel::selectAccount,
                    onBack = navController::navigateUp,
                )
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
                route = AppDestination.PIXELFED_POST_DETAIL,
                arguments = listOf(
                    navArgument(AppDestination.POST_ID_ARGUMENT) {
                        type = NavType.StringType
                    },
                ),
            ) {
                PixelfedPostDetailRoute(
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
                route = AppDestination.LEMMY_POST_DETAIL,
                arguments = listOf(
                    navArgument(AppDestination.POST_ID_ARGUMENT) {
                        type = NavType.StringType
                    },
                ),
            ) {
                LemmyPostDetailRoute(
                    contentPadding = contentPadding,
                    onBack = navController::navigateUp,
                    onUnauthorized = {
                        navController.navigate(AppDestination.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                route = AppDestination.SEARCH_ACCOUNT_PLACEHOLDER,
                arguments = listOf(
                    navArgument(AppDestination.ACCOUNT_ID_ARGUMENT) {
                        type = NavType.StringType
                    },
                ),
            ) { entry ->
                val accountId = entry.arguments
                    ?.getString(AppDestination.ACCOUNT_ID_ARGUMENT)
                    ?.let(Uri::decode)
                    .orEmpty()
                PlaceholderRoute(
                    title = "Account",
                    message = "Profile detail for account $accountId will plug in here.",
                    contentPadding = contentPadding,
                )
            }
            composable(
                route = AppDestination.SEARCH_HASHTAG_PLACEHOLDER,
                arguments = listOf(
                    navArgument(AppDestination.HASHTAG_ARGUMENT) {
                        type = NavType.StringType
                    },
                ),
            ) { entry ->
                val hashtag = entry.arguments
                    ?.getString(AppDestination.HASHTAG_ARGUMENT)
                    ?.let(Uri::decode)
                    .orEmpty()
                PlaceholderRoute(
                    title = "#$hashtag",
                    message = "Hashtag timeline will plug into this route.",
                    contentPadding = contentPadding,
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

private val PlatformType.accentColor
    get() = when (this) {
        PlatformType.MASTODON -> PlatformColors.mastodon
        PlatformType.LEMMY -> PlatformColors.lemmy
        PlatformType.PIXELFED -> PlatformColors.pixelfed
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
