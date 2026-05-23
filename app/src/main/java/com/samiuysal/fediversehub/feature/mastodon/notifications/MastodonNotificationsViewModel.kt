package com.samiuysal.fediversehub.feature.mastodon.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonNotificationMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MastodonNotificationsViewModel @Inject constructor(
    accountStore: AccountStore,
    private val mastodonRepository: MastodonRepository,
) : ViewModel() {
    private val mastodonAccount: Flow<Account> =
        accountStore.accounts
            .map { accounts ->
                accounts.firstOrNull { it.platform == PlatformType.MASTODON }
                    ?: Account(
                        id = "mastodon-notifications-preview",
                        platform = PlatformType.MASTODON,
                        instanceUrl = "mastodon.social",
                        username = "preview",
                        displayName = "Mastodon",
                        avatarUrl = null,
                        accessToken = null,
                    )
            }
            .distinctUntilChanged()

    val notifications: Flow<PagingData<MastodonNotificationUiModel>> =
        mastodonAccount
            .flatMapLatest { account ->
                mastodonRepository.getNotificationsPagingData(account)
            }
            .map { pagingData ->
                pagingData.map(MastodonNotificationMapper::domainToUi)
            }
            .cachedIn(viewModelScope)
}
