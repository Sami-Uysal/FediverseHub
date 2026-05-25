package com.samiuysal.fediversehub.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.userMessage
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchCategory
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
class LemmySearchViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val lemmyRepository: LemmyRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow(LemmySearchCategory.POSTS)
    private val selectedAccountId = MutableStateFlow<String?>(null)

    val queryState: StateFlow<String> = query
    val categoryState: StateFlow<LemmySearchCategory> = category

    val uiState: StateFlow<LemmySearchUiState> =
        combine(
            query.debounce(350).distinctUntilChanged(),
            category,
            selectedAccountId,
            accountStore.accounts,
        ) { queryValue, categoryValue, selectedId, accounts ->
            SearchRequest(
                query = queryValue.trim(),
                category = categoryValue,
                account = accounts.firstOrNull {
                    it.platform == PlatformType.LEMMY && it.id == selectedId
                } ?: accounts.firstOrNull {
                    it.platform == PlatformType.LEMMY && !it.accessToken.isNullOrBlank()
                } ?: PlatformType.LEMMY.publicSearchAccount(),
            )
        }.flatMapLatest { request ->
            flow {
                when {
                    request.query.isBlank() -> emit(LemmySearchUiState.Idle)
                    request.query.length < 2 -> emit(LemmySearchUiState.Idle)
                    else -> {
                        emit(LemmySearchUiState.Loading)
                        when (
                            val result = lemmyRepository.search(
                                account = request.account,
                                query = request.query,
                                category = request.category,
                            )
                        ) {
                            is AppResult.Success -> emit(
                                LemmySearchUiState.Success(LemmySearchUiMapper.domainToUi(result.data)),
                            )
                            is AppResult.Failure -> emit(LemmySearchUiState.Error(result.error.userMessage()))
                        }
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LemmySearchUiState.Idle,
        )

    fun onQueryChanged(value: String) {
        query.value = value
    }

    fun onCategorySelected(value: LemmySearchCategory) {
        category.value = value
    }

    fun selectAccount(account: Account?) {
        selectedAccountId.value = account?.id
    }

    private data class SearchRequest(
        val query: String,
        val category: LemmySearchCategory,
        val account: Account,
    )
}

private fun PlatformType.publicSearchAccount(): Account =
    Account(
        id = "public-lemmy-lemmy.world",
        platform = this,
        instanceUrl = "lemmy.world",
        username = "public",
        displayName = "Lemmy Public",
        avatarUrl = null,
        accessToken = null,
    )

