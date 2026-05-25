package com.samiuysal.fediversehub.feature.notifications

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyNotificationType
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedNotificationType

@Immutable
data class PixelfedNotificationUiModel(
    val id: String,
    val type: PixelfedNotificationType,
    val actorAccountId: String,
    val title: String,
    val actor: String,
    val avatarUrl: String?,
    val postId: String?,
    val preview: String?,
    val time: String,
)

@Immutable
data class LemmyNotificationUiModel(
    val id: String,
    val type: LemmyNotificationType,
    val title: String,
    val actor: String,
    val community: String,
    val postId: String,
    val preview: String,
    val score: Int,
    val time: String,
    val read: Boolean,
)

sealed interface PlatformNotificationUiState<out T> {
    data object NoAccount : PlatformNotificationUiState<Nothing>
    data object Loading : PlatformNotificationUiState<Nothing>
    data class Success<T>(val items: List<T>) : PlatformNotificationUiState<T>
    data class Error(val message: String) : PlatformNotificationUiState<Nothing>
}

enum class LemmyNotificationTab {
    REPLIES,
    MENTIONS,
}
