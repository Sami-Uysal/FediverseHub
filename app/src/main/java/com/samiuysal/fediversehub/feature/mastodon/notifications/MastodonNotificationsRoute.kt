package com.samiuysal.fediversehub.feature.mastodon.notifications

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.samiuysal.fediversehub.core.model.Account

@Composable
fun MastodonNotificationsRoute(
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onPostSelected: (String) -> Unit,
    onProfileSelected: (String) -> Unit,
    viewModel: MastodonNotificationsViewModel = hiltViewModel(),
) {
    androidx.compose.runtime.LaunchedEffect(selectedAccount?.id) {
        viewModel.selectAccount(selectedAccount)
    }
    MastodonNotificationsScreen(
        notifications = viewModel.notifications.collectAsLazyPagingItems(),
        onPostSelected = onPostSelected,
        onProfileSelected = onProfileSelected,
        modifier = Modifier.padding(contentPadding),
    )
}
