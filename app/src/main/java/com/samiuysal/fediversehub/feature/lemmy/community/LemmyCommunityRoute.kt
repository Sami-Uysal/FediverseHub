package com.samiuysal.fediversehub.feature.lemmy.community

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun LemmyCommunityRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onPostSelected: (String) -> Unit,
    onUnauthorized: () -> Unit,
    viewModel: LemmyCommunityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    val posts = viewModel.posts.collectAsLazyPagingItems()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LemmyCommunityEffect.NavigateToLogin -> onUnauthorized()
            }
        }
    }

    LemmyCommunityScreen(
        uiState = uiState,
        posts = posts,
        selectedSort = sort,
        onBack = onBack,
        onRetry = viewModel::retry,
        onSortSelected = viewModel::selectSort,
        onFollowClick = viewModel::toggleFollow,
        onPostSelected = onPostSelected,
        modifier = Modifier.padding(contentPadding),
    )
}
