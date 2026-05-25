package com.samiuysal.fediversehub.feature.profile

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.lemmy.profile.LemmyProfileRoute
import com.samiuysal.fediversehub.feature.mastodon.profile.MastodonProfileRoute
import com.samiuysal.fediversehub.feature.pixelfed.profile.PixelfedProfileRoute

@Composable
fun PlatformProfileRoute(
    selectedPlatform: PlatformType,
    platformAccounts: List<Account>,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    oauthCallbackUri: Uri?,
    onOAuthCallbackConsumed: () -> Unit,
    onPostSelected: (String) -> Unit,
    onPixelfedPostSelected: (String) -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    onPlatformSelected: (PlatformType) -> Unit,
    onAccountSelected: (Account) -> Unit,
    onSettingsClick: () -> Unit,
) {
    when (selectedPlatform) {
        PlatformType.MASTODON -> MastodonProfileRoute(
            selectedPlatform = selectedPlatform,
            platformAccounts = platformAccounts,
            selectedAccount = selectedAccount,
            contentPadding = contentPadding,
            oauthCallbackUri = oauthCallbackUri,
            onOAuthCallbackConsumed = onOAuthCallbackConsumed,
            onPostSelected = onPostSelected,
            onPlatformSelected = onPlatformSelected,
            onAccountSelected = onAccountSelected,
            onSettingsClick = onSettingsClick,
        )
        PlatformType.PIXELFED -> PixelfedProfileRoute(
            selectedPlatform = selectedPlatform,
            platformAccounts = platformAccounts,
            selectedAccount = selectedAccount,
            contentPadding = contentPadding,
            oauthCallbackUri = oauthCallbackUri,
            onOAuthCallbackConsumed = onOAuthCallbackConsumed,
            onPlatformSelected = onPlatformSelected,
            onAccountSelected = onAccountSelected,
            onSettingsClick = onSettingsClick,
            onPostSelected = onPixelfedPostSelected,
            onMediaSelected = onMediaSelected,
        )
        PlatformType.LEMMY -> LemmyProfileRoute(
            selectedPlatform = selectedPlatform,
            platformAccounts = platformAccounts,
            selectedAccount = selectedAccount,
            contentPadding = contentPadding,
            onPostSelected = onPostSelected,
            onPlatformSelected = onPlatformSelected,
            onAccountSelected = onAccountSelected,
            onSettingsClick = onSettingsClick,
        )
    }
}
