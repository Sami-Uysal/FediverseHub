package com.samiuysal.fediversehub.feature.pixelfed.profile

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.PixelfedAuthRoute
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedProfile
import com.samiuysal.fediversehub.feature.profile.ProfilePlatformTopBar

@Composable
fun PixelfedProfileRoute(
    selectedPlatform: PlatformType,
    platformAccounts: List<Account>,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    oauthCallbackUri: Uri?,
    onOAuthCallbackConsumed: () -> Unit,
    onPlatformSelected: (PlatformType) -> Unit,
    onAccountSelected: (Account) -> Unit,
    onSettingsClick: () -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    viewModel: PixelfedProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val media = viewModel.profileMedia.collectAsLazyPagingItems()

    LaunchedEffect(selectedAccount?.id) {
        viewModel.selectAccount(selectedAccount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        ProfilePlatformTopBar(
            selectedPlatform = selectedPlatform,
            platformAccounts = platformAccounts,
            selectedAccount = selectedAccount,
            onPlatformSelected = onPlatformSelected,
            onAccountSelected = onAccountSelected,
            onSettingsClick = onSettingsClick,
        )
        when (val state = uiState) {
            PixelfedProfileUiState.NoAccount -> PixelfedAuthRoute(
                oauthCallbackUri = oauthCallbackUri,
                onOAuthCallbackConsumed = onOAuthCallbackConsumed,
            )
            PixelfedProfileUiState.Loading -> AppLoading(
                message = "Loading Pixelfed profile...",
                modifier = Modifier.fillMaxSize(),
            )
            is PixelfedProfileUiState.Error -> AppErrorState(
                message = state.message,
                onRetry = media::refresh,
                modifier = Modifier.fillMaxSize(),
            )
            is PixelfedProfileUiState.Success -> PixelfedProfileContent(
                profile = state.profile,
                media = media,
                onMediaSelected = onMediaSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PixelfedProfileContent(
    profile: PixelfedProfile,
    media: androidx.paging.compose.LazyPagingItems<PixelfedPostUiModel>,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            PixelfedProfileHeader(profile = profile)
        }
        when {
            media.loadState.refresh is LoadState.Loading && media.itemCount == 0 -> {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    AppLoading(message = "Loading media...", modifier = Modifier.height(160.dp))
                }
            }
            media.loadState.refresh is LoadState.Error && media.itemCount == 0 -> {
                val error = media.loadState.refresh as LoadState.Error
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    AppErrorState(
                        message = error.error.localizedMessage ?: "Profile media failed.",
                        onRetry = media::retry,
                        modifier = Modifier.height(180.dp),
                    )
                }
            }
            media.itemCount == 0 -> {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        title = "No media",
                        message = "No public Pixelfed posts visible yet.",
                        modifier = Modifier.height(180.dp),
                    )
                }
            }
        }
        items(
            count = media.itemCount,
            key = media.itemKey { it.id },
            contentType = media.itemContentType { "pixelfed-profile-media" },
        ) { index ->
            media[index]?.let { post ->
                PixelfedGridImage(
                    post = post,
                    onClick = {
                        onMediaSelected(
                            post.fullImageUrls.ifEmpty { listOf(post.imageUrl) },
                            post.altFlags,
                            0,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun PixelfedProfileHeader(profile: PixelfedProfile) {
    Column {
        profile.headerUrl?.let { headerUrl ->
            val context = LocalContext.current
            val request = remember(context, headerUrl) {
                ImageRequest.Builder(context)
                    .data(headerUrl)
                    .size(720, 180)
                    .precision(Precision.INEXACT)
                    .crossfade(false)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                AsyncImage(
                    model = request,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppAvatar(
                imageUrl = profile.avatarUrl,
                name = profile.displayName,
                size = 58.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = profile.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(AppSpacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    ProfileCount(profile.statusesCount, "posts")
                    ProfileCount(profile.followersCount, "followers")
                    ProfileCount(profile.followingCount, "following")
                }
            }
        }
        if (profile.note.isNotBlank()) {
            Text(
                text = profile.note,
                modifier = Modifier.padding(start = AppSpacing.lg, end = AppSpacing.lg, bottom = AppSpacing.sm),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun ProfileCount(value: Int, label: String) {
    Column {
        Text("$value", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PixelfedGridImage(
    post: PixelfedPostUiModel,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val request = remember(context, post.imageUrl) {
        ImageRequest.Builder(context)
            .data(post.imageUrl)
            .size(GRID_IMAGE_SIZE, GRID_IMAGE_SIZE)
            .precision(Precision.INEXACT)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.sm))
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop,
    )
}

private const val GRID_IMAGE_SIZE = 240
