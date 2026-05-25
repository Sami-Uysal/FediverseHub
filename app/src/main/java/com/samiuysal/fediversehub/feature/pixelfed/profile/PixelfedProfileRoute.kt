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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
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
import com.samiuysal.fediversehub.core.common.error.userFacingMessage
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
    onPostSelected: (String) -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    viewModel: PixelfedProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val followState by viewModel.followState.collectAsStateWithLifecycle()
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
                oauthCallbackUri = null,
                onOAuthCallbackConsumed = {},
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
                followState = followState,
                onFollowClick = viewModel::toggleFollow,
                media = media,
                onPostSelected = onPostSelected,
                onMediaSelected = onMediaSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun PixelfedAccountProfileRoute(
    accountId: String,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onPostSelected: (String) -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    viewModel: PixelfedProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val followState by viewModel.followState.collectAsStateWithLifecycle()
    val media = viewModel.profileMedia.collectAsLazyPagingItems()

    LaunchedEffect(selectedAccount?.id, accountId) {
        viewModel.selectRemoteAccount(selectedAccount, accountId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(end = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
            }
            Text(
                text = "Pixelfed profil",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        when (val state = uiState) {
            PixelfedProfileUiState.NoAccount -> EmptyState(
                title = "Profil açılamadı",
                message = "Bu Pixelfed profili şu an yüklenemedi.",
                modifier = Modifier.fillMaxSize(),
            )
            PixelfedProfileUiState.Loading -> AppLoading(
                message = "Pixelfed profili yükleniyor...",
                modifier = Modifier.fillMaxSize(),
            )
            is PixelfedProfileUiState.Error -> AppErrorState(
                message = state.message,
                onRetry = media::refresh,
                modifier = Modifier.fillMaxSize(),
            )
            is PixelfedProfileUiState.Success -> PixelfedProfileContent(
                profile = state.profile,
                followState = followState,
                onFollowClick = viewModel::toggleFollow,
                media = media,
                onPostSelected = onPostSelected,
                onMediaSelected = onMediaSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PixelfedProfileContent(
    profile: PixelfedProfile,
    followState: PixelfedFollowUiState,
    onFollowClick: () -> Unit,
    media: androidx.paging.compose.LazyPagingItems<PixelfedPostUiModel>,
    onPostSelected: (String) -> Unit,
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
            PixelfedProfileHeader(
                profile = profile,
                followState = followState,
                onFollowClick = onFollowClick,
            )
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
                        message = error.error.userFacingMessage("Profil medyası yüklenemedi."),
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
                        onPostSelected(post.id)
                    },
                )
            }
        }
        when (val appendState = media.loadState.append) {
            LoadState.Loading -> {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    AppLoading(message = "Loading more media...", modifier = Modifier.height(96.dp))
                }
            }
            is LoadState.Error -> {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    AppErrorState(
                        message = appendState.error.userFacingMessage("Daha fazla medya yüklenemedi."),
                        onRetry = media::retry,
                        modifier = Modifier.height(120.dp),
                    )
                }
            }
            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun PixelfedProfileHeader(
    profile: PixelfedProfile,
    followState: PixelfedFollowUiState,
    onFollowClick: () -> Unit,
) {
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
            PixelfedFollowButton(state = followState, onClick = onFollowClick)
        }
        followState.errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(start = AppSpacing.lg, end = AppSpacing.lg, bottom = AppSpacing.sm),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
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
private fun PixelfedFollowButton(
    state: PixelfedFollowUiState,
    onClick: () -> Unit,
) {
    if (state.isOwnProfile) return
    val content: @Composable () -> Unit = {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Text(if (state.isFollowing) "Unfollow" else "Follow")
        }
    }
    if (state.isFollowing) {
        OutlinedButton(onClick = onClick, enabled = !state.isLoading) {
            content()
        }
    } else {
        Button(onClick = onClick, enabled = !state.isLoading) {
            content()
        }
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
