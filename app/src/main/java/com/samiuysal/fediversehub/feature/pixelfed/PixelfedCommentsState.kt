package com.samiuysal.fediversehub.feature.pixelfed

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedComment

@Immutable
data class PixelfedCommentsState(
    val postId: String,
    val isLoading: Boolean = false,
    val comments: List<PixelfedComment> = emptyList(),
    val errorMessage: String? = null,
)
