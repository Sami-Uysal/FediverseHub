package com.samiuysal.fediversehub.feature.notifications

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.mastodon.notifications.MastodonNotificationsRoute

@Composable
fun PlatformNotificationsRoute(
    selectedPlatform: PlatformType,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onPostSelected: (String) -> Unit,
    onProfileSelected: (String) -> Unit,
) {
    when (selectedPlatform) {
        PlatformType.MASTODON -> MastodonNotificationsRoute(
            selectedAccount = selectedAccount,
            contentPadding = contentPadding,
            onPostSelected = onPostSelected,
            onProfileSelected = onProfileSelected,
        )
        PlatformType.LEMMY -> EmptyState(
            title = "Lemmy bildirimleri yakında",
            message = "Community yanıtları ve mention'lar burada görünecek.",
            modifier = Modifier.padding(contentPadding),
        )
        PlatformType.PIXELFED -> EmptyState(
            title = "Pixelfed bildirimleri yakında",
            message = "Beğeniler, yorumlar ve takipler burada görünecek.",
            modifier = Modifier.padding(contentPadding),
        )
    }
}
