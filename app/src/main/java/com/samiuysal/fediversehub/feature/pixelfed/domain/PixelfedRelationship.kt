package com.samiuysal.fediversehub.feature.pixelfed.domain

data class PixelfedRelationship(
    val accountId: String,
    val following: Boolean,
    val requested: Boolean,
)
