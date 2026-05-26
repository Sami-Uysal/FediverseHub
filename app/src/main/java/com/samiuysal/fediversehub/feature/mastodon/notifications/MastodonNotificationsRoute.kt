package com.samiuysal.fediversehub.feature.mastodon.notifications

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType

@Composable
fun MastodonNotificationsRoute(
    selectedPlatform: PlatformType,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    onPostSelected: (String) -> Unit,
    onProfileSelected: (String) -> Unit,
    viewModel: MastodonNotificationsViewModel = hiltViewModel(),
) {
    androidx.compose.runtime.LaunchedEffect(selectedAccount?.id) {
        viewModel.selectAccount(selectedAccount)
    }
    MastodonNotificationsScreen(
        selectedPlatform = selectedPlatform,
        notifications = viewModel.notifications.collectAsLazyPagingItems(),
        onPlatformSelected = onPlatformSelected,
        onPostSelected = onPostSelected,
        onProfileSelected = onProfileSelected,
        modifier = Modifier.padding(contentPadding),
    )
}
