package com.samiuysal.fediversehub.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppScaffold
import com.samiuysal.fediversehub.core.designsystem.theme.PlatformColors
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.core.performance.PerfLogger
import com.samiuysal.fediversehub.feature.auth.MastodonAuthRoute
import com.samiuysal.fediversehub.feature.auth.PixelfedAuthRoute
import com.samiuysal.fediversehub.feature.explore.ExploreRoute
import com.samiuysal.fediversehub.feature.home.HomeRoute
import com.samiuysal.fediversehub.feature.lemmy.community.LemmyCommunityRoute
import com.samiuysal.fediversehub.feature.lemmy.detail.LemmyPostDetailRoute
import com.samiuysal.fediversehub.feature.lemmy.profile.LemmyUserProfileRoute
import com.samiuysal.fediversehub.feature.mastodon.detail.MastodonPostDetailRoute
import com.samiuysal.fediversehub.feature.mastodon.media.FullScreenMediaViewer
import com.samiuysal.fediversehub.feature.mastodon.searchdetail.MastodonAccountDetailRoute
import com.samiuysal.fediversehub.feature.mastodon.searchdetail.MastodonHashtagTimelineRoute
import com.samiuysal.fediversehub.feature.notifications.PlatformNotificationsRoute
import com.samiuysal.fediversehub.feature.onboarding.OnboardingScreen
import com.samiuysal.fediversehub.feature.pixelfed.detail.PixelfedPostDetailRoute
import com.samiuysal.fediversehub.feature.pixelfed.profile.PixelfedAccountProfileRoute
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
    var onboardingTargetRoute by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        PerfLogger.log("app_start_compose")
    }

    LaunchedEffect(oauthCallbackUri, appState.onboardingSeen) {
        if (oauthCallbackUri != null && appState.onboardingSeen != null) {
            val targetRoute = when (oauthCallbackUri.path) {
                "/mastodon" -> {
                    appStateViewModel.selectPlatform(PlatformType.MASTODON)
                    AppDestination.AUTH_MASTODON
                }
                "/pixelfed" -> {
                    appStateViewModel.selectPlatform(PlatformType.PIXELFED)
                    AppDestination.AUTH_PIXELFED
                }
                else -> null
            }
            if (targetRoute != null) {
                navController.navigate(targetRoute) {
                    launchSingleTop = true
                }
            }
        }
    }

    LaunchedEffect(appState.onboardingSeen, onboardingTargetRoute) {
        val targetRoute = onboardingTargetRoute
        if (appState.onboardingSeen == true && targetRoute != null) {
            navController.navigate(targetRoute) {
                launchSingleTop = true
                popUpTo(AppDestination.HOME)
            }
            onboardingTargetRoute = null
        }
    }

    if (appState.onboardingSeen == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AppLoading(message = "Hazırlanıyor...")
        }
        return
    }

    if (appState.onboardingSeen == false && oauthCallbackUri == null) {
        OnboardingScreen(
            selectedPlatform = appState.selectedPlatform,
            onPlatformSelected = appStateViewModel::selectPlatform,
            onAddAccount = {
                onboardingTargetRoute = AppDestination.PROFILE
                appStateViewModel.completeOnboarding()
            },
            onExplore = {
                onboardingTargetRoute = AppDestination.EXPLORE
                appStateViewModel.completeOnboarding()
            },
        )
        return
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
                    onMastodonAccountSelected = { accountId ->
                        navController.navigate(AppDestination.mastodonAccountDetail(accountId))
                    },
                    onPixelfedPostSelected = { postId ->
                        navController.navigate(AppDestination.pixelfedPostDetail(Uri.encode(postId)))
                    },
                    onPixelfedAccountSelected = { accountId ->
                        navController.navigate(AppDestination.pixelfedAccountDetail(accountId))
                    },
                    onLemmyPostSelected = { postId ->
                        navController.navigate(AppDestination.lemmyPostDetail(Uri.encode(postId)))
                    },
                    onLemmyCommunitySelected = { communityName ->
                        navController.navigate(AppDestination.lemmyCommunity(communityName))
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
                    onPixelfedPostSelected = { postId ->
                        navController.navigate(AppDestination.pixelfedPostDetail(Uri.encode(postId)))
                    },
                    onLemmyPostSelected = { postId ->
                        navController.navigate(AppDestination.lemmyPostDetail(Uri.encode(postId)))
                    },
                    onAccountSelected = { accountId ->
                        navController.navigate(AppDestination.mastodonAccountDetail(accountId))
                    },
                    onPixelfedAccountSelected = {
                        navController.navigate(AppDestination.pixelfedAccountDetail(it))
                    },
                    onLemmyCommunitySelected = { communityName ->
                        navController.navigate(AppDestination.lemmyCommunity(communityName))
                    },
                    onLemmyUserSelected = {
                        navController.navigate(AppDestination.lemmyUserDetail(it))
                    },
                    onHashtagSelected = { hashtag ->
                        navController.navigate(AppDestination.mastodonHashtagTimeline(hashtag))
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
                    onLemmyCommunitySelected = { communityName ->
                        navController.navigate(AppDestination.lemmyCommunity(communityName))
                    },
                    onMastodonAccountSelected = { accountId ->
                        navController.navigate(AppDestination.mastodonAccountDetail(accountId))
                    },
                    onHashtagSelected = { hashtag ->
                        navController.navigate(AppDestination.mastodonHashtagTimeline(hashtag))
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
                    onProfileSelected = { accountId ->
                        when (appState.selectedPlatform) {
                            PlatformType.MASTODON -> navController.navigate(AppDestination.mastodonAccountDetail(accountId))
                            PlatformType.PIXELFED -> navController.navigate(AppDestination.pixelfedAccountDetail(accountId))
                            PlatformType.LEMMY -> navController.navigate(AppDestination.PROFILE) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onPixelfedPostSelected = { postId ->
                        navController.navigate(AppDestination.pixelfedPostDetail(Uri.encode(postId)))
                    },
                    onLemmyPostSelected = { postId ->
                        navController.navigate(AppDestination.lemmyPostDetail(Uri.encode(postId)))
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
                        onLoginCompleted = {
                            navController.navigate(AppDestination.PROFILE) {
                                launchSingleTop = true
                                popUpTo(AppDestination.AUTH_MASTODON) {
                                    inclusive = true
                                }
                            }
                        },
                    )
                }
            }
            composable(AppDestination.AUTH_PIXELFED) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.padding(contentPadding),
                ) {
                    PixelfedAuthRoute(
                        oauthCallbackUri = oauthCallbackUri,
                        onOAuthCallbackConsumed = onOAuthCallbackConsumed,
                        onLoginCompleted = {
                            navController.navigate(AppDestination.PROFILE) {
                                launchSingleTop = true
                                popUpTo(AppDestination.AUTH_PIXELFED) {
                                    inclusive = true
                                }
                            }
                        },
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
                    onAccountSelected = { accountId ->
                        navController.navigate(AppDestination.mastodonAccountDetail(accountId))
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
                    onAccountSelected = { accountId ->
                        navController.navigate(AppDestination.pixelfedAccountDetail(accountId))
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
                    onCommunitySelected = { communityName ->
                        navController.navigate(AppDestination.lemmyCommunity(communityName))
                    },
                    onUnauthorized = {
                        navController.navigate(AppDestination.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                route = AppDestination.LEMMY_COMMUNITY_DETAIL,
                arguments = listOf(
                    navArgument(AppDestination.COMMUNITY_NAME_ARGUMENT) {
                        type = NavType.StringType
                    },
                ),
            ) {
                LemmyCommunityRoute(
                    contentPadding = contentPadding,
                    onBack = navController::navigateUp,
                    onPostSelected = { postId ->
                        navController.navigate(AppDestination.lemmyPostDetail(Uri.encode(postId)))
                    },
                    onUnauthorized = {
                        navController.navigate(AppDestination.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                route = AppDestination.PIXELFED_ACCOUNT_DETAIL,
                arguments = listOf(
                    navArgument(AppDestination.ACCOUNT_ID_ARGUMENT) {
                        type = NavType.StringType
                    },
                ),
            ) { entry ->
                val accountId = entry.arguments?.getString(AppDestination.ACCOUNT_ID_ARGUMENT).orEmpty()
                PixelfedAccountProfileRoute(
                    accountId = accountId,
                    selectedAccount = appState.selectedAccount,
                    contentPadding = contentPadding,
                    onBack = navController::navigateUp,
                    onPostSelected = { postId ->
                        navController.navigate(AppDestination.pixelfedPostDetail(Uri.encode(postId)))
                    },
                    onMediaSelected = { urls, altFlags, index ->
                        navController.navigate(AppDestination.mastodonMediaViewer(urls, altFlags, index))
                    },
                )
            }
            composable(
                route = AppDestination.LEMMY_USER_DETAIL,
                arguments = listOf(
                    navArgument(AppDestination.USERNAME_ARGUMENT) {
                        type = NavType.StringType
                    },
                ),
            ) { entry ->
                val username = entry.arguments?.getString(AppDestination.USERNAME_ARGUMENT).orEmpty()
                LemmyUserProfileRoute(
                    username = username,
                    selectedAccount = appState.selectedAccount,
                    contentPadding = contentPadding,
                    onBack = navController::navigateUp,
                    onPostSelected = { postId ->
                        navController.navigate(AppDestination.lemmyPostDetail(Uri.encode(postId)))
                    },
                )
            }
            composable(
                route = AppDestination.MASTODON_ACCOUNT_DETAIL,
                arguments = listOf(
                    navArgument(AppDestination.ACCOUNT_ID_ARGUMENT) {
                        type = NavType.StringType
                    },
                ),
            ) { entry ->
                MastodonAccountDetailRoute(
                    contentPadding = contentPadding,
                    onBack = navController::navigateUp,
                    onPostSelected = { postId ->
                        navController.navigate(AppDestination.mastodonPostDetail(Uri.encode(postId)))
                    },
                    onAccountSelected = { accountId ->
                        navController.navigate(AppDestination.mastodonAccountDetail(accountId))
                    },
                )
            }
            composable(
                route = AppDestination.MASTODON_HASHTAG_TIMELINE,
                arguments = listOf(
                    navArgument(AppDestination.HASHTAG_ARGUMENT) {
                        type = NavType.StringType
                    },
                ),
            ) { entry ->
                MastodonHashtagTimelineRoute(
                    contentPadding = contentPadding,
                    onBack = navController::navigateUp,
                    onPostSelected = { postId ->
                        navController.navigate(AppDestination.mastodonPostDetail(Uri.encode(postId)))
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

private val PlatformType.accentColor
    get() = when (this) {
        PlatformType.MASTODON -> PlatformColors.mastodon
        PlatformType.LEMMY -> PlatformColors.lemmy
        PlatformType.PIXELFED -> PlatformColors.pixelfed
    }
