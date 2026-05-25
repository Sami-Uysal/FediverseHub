package com.samiuysal.fediversehub.feature.search

import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchResult
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyPostMapper
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedSearchResult
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper

object PixelfedSearchUiMapper {
    fun domainToUi(domain: PixelfedSearchResult): PixelfedSearchResultsUiModel =
        PixelfedSearchResultsUiModel(
            posts = domain.posts.map(PixelfedMapper::postToUi),
            accounts = domain.accounts.map {
                PixelfedSearchAccountUiModel(
                    id = it.id,
                    displayName = it.displayName,
                    username = it.username,
                    avatarUrl = it.avatarUrl,
                    note = it.note,
                )
            },
            hashtags = domain.hashtags.map { PixelfedHashtagUiModel(it.name) },
        )
}

object LemmySearchUiMapper {
    fun domainToUi(domain: LemmySearchResult): LemmySearchResultsUiModel =
        LemmySearchResultsUiModel(
            posts = domain.posts.map(LemmyPostMapper::domainToUi),
            communities = domain.communities.map(LemmyPostMapper::communityToUi),
            users = domain.users.map {
                LemmySearchUserUiModel(
                    id = it.id,
                    name = it.name,
                    displayName = it.displayName,
                    avatarUrl = it.avatarUrl,
                    bio = it.bio,
                )
            },
        )
}

