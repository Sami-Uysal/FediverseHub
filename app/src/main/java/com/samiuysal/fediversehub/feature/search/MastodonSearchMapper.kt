package com.samiuysal.fediversehub.feature.search

import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonHashtag
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchAccount
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchResult
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper

object MastodonSearchUiMapper {
    fun domainToUi(domain: MastodonSearchResult): MastodonSearchResultsUiModel =
        MastodonSearchResultsUiModel(
            posts = domain.posts.map(MastodonTimelineMapper::domainToUi),
            accounts = domain.accounts.map(::accountToUi),
            hashtags = domain.hashtags.map(::hashtagToUi),
        )

    private fun accountToUi(account: MastodonSearchAccount): MastodonSearchAccountUiModel =
        MastodonSearchAccountUiModel(
            id = account.id,
            displayName = account.displayName,
            username = account.username,
            avatarUrl = account.avatarUrl,
            note = account.note,
        )

    private fun hashtagToUi(hashtag: MastodonHashtag): MastodonHashtagUiModel =
        MastodonHashtagUiModel(name = hashtag.name)
}
