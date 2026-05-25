package com.samiuysal.fediversehub.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.userMessage
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRepository
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedSearchCategory
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
class PixelfedSearchViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val pixelfedRepository: PixelfedRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow(PixelfedSearchCategory.POSTS)
    private val selectedAccountId = MutableStateFlow<String?>(null)

    val queryState: StateFlow<String> = query
    val categoryState: StateFlow<PixelfedSearchCategory> = category

    val uiState: StateFlow<PixelfedSearchUiState> =
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
                    it.platform == PlatformType.PIXELFED && it.id == selectedId
                } ?: accounts.firstOrNull {
                    it.platform == PlatformType.PIXELFED && !it.accessToken.isNullOrBlank()
                } ?: PlatformType.PIXELFED.publicSearchAccount(),
            )
        }.flatMapLatest { request ->
            flow {
                when {
                    request.query.isBlank() -> emit(PixelfedSearchUiState.Idle)
                    request.query.length < 2 -> emit(PixelfedSearchUiState.Idle)
                    else -> {
                        emit(PixelfedSearchUiState.Loading)
                        when (
                            val result = pixelfedRepository.search(
                                account = request.account,
                                query = request.query,
                                category = request.category,
                            )
                        ) {
                            is AppResult.Success -> emit(
                                PixelfedSearchUiState.Success(PixelfedSearchUiMapper.domainToUi(result.data)),
                            )
                            is AppResult.Failure -> emit(PixelfedSearchUiState.Error(result.error.userMessage()))
                        }
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PixelfedSearchUiState.Idle,
        )

    fun onQueryChanged(value: String) {
        query.value = value
    }

    fun onCategorySelected(value: PixelfedSearchCategory) {
        category.value = value
    }

    fun selectAccount(account: Account?) {
        selectedAccountId.value = account?.id
    }

    private data class SearchRequest(
        val query: String,
        val category: PixelfedSearchCategory,
        val account: Account,
    )
}

private fun PlatformType.publicSearchAccount(): Account =
    Account(
        id = "public-pixelfed-pixelfed.social",
        platform = this,
        instanceUrl = "pixelfed.social",
        username = "public",
        displayName = "Pixelfed Public",
        avatarUrl = null,
        accessToken = null,
    )

