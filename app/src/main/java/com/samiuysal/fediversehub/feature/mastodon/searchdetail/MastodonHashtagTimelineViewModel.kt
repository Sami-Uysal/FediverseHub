package com.samiuysal.fediversehub.feature.mastodon.searchdetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import com.samiuysal.fediversehub.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MastodonHashtagTimelineViewModel @Inject constructor(
    accountStore: AccountStore,
    mastodonRepository: MastodonRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val hashtag: String = Uri.decode(checkNotNull(savedStateHandle[AppDestination.HASHTAG_ARGUMENT]))
        .removePrefix("#")

    private val mastodonAccount: Flow<Account?> = accountStore.accounts
        .map { accounts ->
            accounts.firstOrNull {
                it.platform == PlatformType.MASTODON && !it.accessToken.isNullOrBlank()
            }
        }
        .distinctUntilChanged()

    val hasAccount: StateFlow<Boolean> = mastodonAccount
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    val posts: Flow<PagingData<MastodonPostUiModel>> =
        mastodonAccount
            .flatMapLatest { account ->
                if (account == null) {
                    flow { emit(PagingData.empty()) }
                } else {
                    mastodonRepository.getHashtagTimelinePagingData(
                        account = account,
                        hashtag = hashtag,
                    )
                }
            }
            .map { pagingData -> pagingData.map(MastodonTimelineMapper::domainToUi) }
            .cachedIn(viewModelScope)
}
