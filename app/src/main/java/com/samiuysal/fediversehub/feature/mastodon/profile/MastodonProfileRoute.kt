package com.samiuysal.fediversehub.feature.mastodon.profile

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.MastodonAuthRoute
import com.samiuysal.fediversehub.feature.profile.ProfilePlatformTopBar

@Composable
fun MastodonProfileRoute(
    selectedPlatform: PlatformType,
    platformAccounts: List<Account>,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    oauthCallbackUri: Uri?,
    onOAuthCallbackConsumed: () -> Unit,
    onPostSelected: (String) -> Unit,
    onPlatformSelected: (PlatformType) -> Unit,
    onAccountSelected: (Account) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MastodonProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val posts = viewModel.profilePosts.collectAsLazyPagingItems()

    androidx.compose.runtime.LaunchedEffect(selectedAccount?.id) {
        viewModel.selectAccount(selectedAccount)
    }

    when (uiState) {
        MastodonProfileUiState.NoAccount -> {
            Column(
                modifier = Modifier.padding(contentPadding),
            ) {
                ProfilePlatformTopBar(
                    selectedPlatform = selectedPlatform,
                    platformAccounts = platformAccounts,
                    selectedAccount = selectedAccount,
                    onPlatformSelected = onPlatformSelected,
                    onAccountSelected = onAccountSelected,
                    onSettingsClick = onSettingsClick,
                )
                MastodonAuthRoute(
                    oauthCallbackUri = oauthCallbackUri,
                    onOAuthCallbackConsumed = onOAuthCallbackConsumed,
                )
            }
        }
        else -> MastodonProfileScreen(
            uiState = uiState,
            selectedPlatform = selectedPlatform,
            platformAccounts = platformAccounts,
            selectedAccount = selectedAccount,
            posts = posts,
            onFilterSelected = viewModel::selectFilter,
            onPostSelected = onPostSelected,
            onPlatformSelected = onPlatformSelected,
            onAccountSelected = onAccountSelected,
            onSettingsClick = onSettingsClick,
            modifier = Modifier.padding(contentPadding),
        )
    }
}
