package com.samiuysal.fediversehub.feature.mastodon.mapper

import androidx.core.text.HtmlCompat
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAccountDto
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfile
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfileField
import com.samiuysal.fediversehub.feature.mastodon.profile.MastodonProfileFieldUiModel
import com.samiuysal.fediversehub.feature.mastodon.profile.MastodonProfileUiModel

object MastodonProfileMapper {
    fun dtoToDomain(dto: MastodonAccountDto): MastodonProfile =
        MastodonProfile(
            id = dto.id,
            displayName = htmlToPlainText(dto.displayName.ifBlank { dto.username }),
            username = "@${dto.acct.ifBlank { dto.username }}",
            avatarUrl = dto.avatarStatic ?: dto.avatar,
            headerUrl = dto.headerStatic ?: dto.header,
            note = htmlToPlainText(dto.note),
            followersCount = dto.followersCount,
            followingCount = dto.followingCount,
            statusesCount = dto.statusesCount,
            fields = dto.fields.map {
                MastodonProfileField(
                    name = htmlToPlainText(it.name),
                    value = htmlToPlainText(it.value),
                )
            },
        )

    fun domainToUi(domain: MastodonProfile): MastodonProfileUiModel =
        MastodonProfileUiModel(
            id = domain.id,
            displayName = domain.displayName,
            username = domain.username,
            avatarUrl = domain.avatarUrl,
            headerUrl = domain.headerUrl,
            note = domain.note,
            followersCount = domain.followersCount,
            followingCount = domain.followingCount,
            statusesCount = domain.statusesCount,
            fields = domain.fields.map {
                MastodonProfileFieldUiModel(
                    name = it.name,
                    value = it.value,
                )
            },
        )

    private fun htmlToPlainText(value: String): String =
        HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString()
            .trim()
}
