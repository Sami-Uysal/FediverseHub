package com.samiuysal.fediversehub.feature.mastodon.mapper

import androidx.core.text.HtmlCompat
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAccountDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonHashtagDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonSearchDto
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonHashtag
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchAccount
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchResult

object MastodonSearchMapper {
    fun dtoToDomain(dto: MastodonSearchDto): MastodonSearchResult =
        MastodonSearchResult(
            posts = dto.statuses.map(MastodonTimelineMapper::dtoToDomain),
            accounts = dto.accounts.map(::accountDtoToDomain),
            hashtags = dto.hashtags.map(::hashtagDtoToDomain),
        )

    private fun accountDtoToDomain(dto: MastodonAccountDto): MastodonSearchAccount =
        MastodonSearchAccount(
            id = dto.id,
            displayName = htmlToPlainText(dto.displayName.ifBlank { dto.username }),
            username = "@${dto.acct.ifBlank { dto.username }}",
            avatarUrl = dto.avatarStatic ?: dto.avatar,
            note = htmlToPlainText(dto.note),
        )

    private fun hashtagDtoToDomain(dto: MastodonHashtagDto): MastodonHashtag =
        MastodonHashtag(
            name = "#${dto.name.removePrefix("#")}",
            url = dto.url,
        )

    private fun htmlToPlainText(value: String): String =
        HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString()
            .trim()
}
