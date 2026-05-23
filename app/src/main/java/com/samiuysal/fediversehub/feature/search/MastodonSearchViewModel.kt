package com.samiuysal.fediversehub.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class MastodonSearchViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val mastodonRepository: MastodonRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow(MastodonSearchCategory.POSTS)
    private val selectedAccountId = MutableStateFlow<String?>(null)

    val queryState: StateFlow<String> = query
    val categoryState: StateFlow<MastodonSearchCategory> = category

    val uiState: StateFlow<MastodonSearchUiState> =
        combine(
            query.debounce(350).distinctUntilChanged(),
            category,
            selectedAccountId,
            accountStore.accounts,
        ) { queryValue, categoryValue, activeAccountId, accounts ->
            SearchRequest(
                query = queryValue.trim(),
                category = categoryValue,
                account = accounts.firstOrNull {
                    it.platform == PlatformType.MASTODON &&
                        it.id == activeAccountId &&
                        !it.accessToken.isNullOrBlank()
                } ?: accounts.firstOrNull {
                    it.platform == PlatformType.MASTODON && !it.accessToken.isNullOrBlank()
                },
            )
        }.flatMapLatest { request ->
            flow {
                when {
                    request.query.isBlank() -> emit(MastodonSearchUiState.Idle)
                    request.account == null -> emit(
                        MastodonSearchUiState.Error("Log in to Mastodon to search."),
                    )
                    else -> {
                        emit(MastodonSearchUiState.Loading)
                        when (
                            val result = mastodonRepository.search(
                                account = request.account,
                                query = request.query,
                                category = request.category,
                            )
                        ) {
                            is AppResult.Success -> emit(
                                MastodonSearchUiState.Success(
                                    MastodonSearchUiMapper.domainToUi(result.data),
                                ),
                            )
                            is AppResult.Failure -> emit(
                                MastodonSearchUiState.Error(result.error.searchMessage()),
                            )
                        }
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MastodonSearchUiState.Idle,
        )

    fun onQueryChanged(value: String) {
        query.value = value
    }

    fun onCategorySelected(value: MastodonSearchCategory) {
        category.value = value
    }

    fun selectAccount(account: Account?) {
        selectedAccountId.value = account?.id
    }

    private data class SearchRequest(
        val query: String,
        val category: MastodonSearchCategory,
        val account: Account?,
    )

    private fun AppError.searchMessage(): String = when (this) {
        AppError.Unauthorized -> "Session expired. Log in again to search."
        AppError.RateLimited -> "Rate limit reached. Wait a moment, then retry."
        AppError.Network -> "Network failed. Check your connection and retry."
        is AppError.Server -> "Server error $code. Try again shortly."
        is AppError.Unknown -> message ?: "Search failed. Try again."
    }
}
