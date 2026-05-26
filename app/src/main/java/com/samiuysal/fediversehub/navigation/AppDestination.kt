package com.samiuysal.fediversehub.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import com.samiuysal.fediversehub.core.designsystem.component.AppBottomNavItem

object AppDestination {
    const val HOME = "home"
    const val SEARCH = "search"
    const val EXPLORE = "explore"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val AUTH_MASTODON = "auth/mastodon"
    const val AUTH_PIXELFED = "auth/pixelfed"
    const val POST_ID_ARGUMENT = "postId"
    const val MEDIA_URLS_ARGUMENT = "urls"
    const val MEDIA_ALTS_ARGUMENT = "alts"
    const val MEDIA_INDEX_ARGUMENT = "index"
    const val ACCOUNT_ID_ARGUMENT = "accountId"
    const val USERNAME_ARGUMENT = "username"
    const val HASHTAG_ARGUMENT = "hashtag"
    const val COMMUNITY_NAME_ARGUMENT = "communityName"
    const val MASTODON_POST_DETAIL = "mastodon/post/{$POST_ID_ARGUMENT}"
    const val PIXELFED_POST_DETAIL = "pixelfed/post/{$POST_ID_ARGUMENT}"
    const val LEMMY_POST_DETAIL = "lemmy/post/{$POST_ID_ARGUMENT}"
    const val LEMMY_COMMUNITY_DETAIL = "lemmy/community/{$COMMUNITY_NAME_ARGUMENT}"
    const val MASTODON_MEDIA_VIEWER =
        "mastodon/media?urls={$MEDIA_URLS_ARGUMENT}&alts={$MEDIA_ALTS_ARGUMENT}&index={$MEDIA_INDEX_ARGUMENT}"
    const val MASTODON_ACCOUNT_DETAIL = "search/account/{$ACCOUNT_ID_ARGUMENT}"
    const val PIXELFED_ACCOUNT_DETAIL = "pixelfed/account/{$ACCOUNT_ID_ARGUMENT}"
    const val LEMMY_USER_DETAIL = "lemmy/user/{$USERNAME_ARGUMENT}"
    const val MASTODON_HASHTAG_TIMELINE = "search/hashtag/{$HASHTAG_ARGUMENT}"

    val bottomNavItems = listOf(
        AppBottomNavItem(HOME, "Home", Icons.Outlined.Home),
        AppBottomNavItem(SEARCH, "Search", Icons.Outlined.Search),
        AppBottomNavItem(EXPLORE, "Keşfet", Icons.Outlined.Explore),
        AppBottomNavItem(PROFILE, "Profile", Icons.Outlined.Person),
    )

    fun mastodonPostDetail(postId: String): String = "mastodon/post/$postId"

    fun pixelfedPostDetail(postId: String): String = "pixelfed/post/$postId"

    fun lemmyPostDetail(postId: String): String = "lemmy/post/$postId"

    fun lemmyCommunity(communityName: String): String = "lemmy/community/${Uri.encode(communityName)}"

    fun mastodonAccountDetail(accountId: String): String =
        "search/account/${Uri.encode(accountId)}"

    fun pixelfedAccountDetail(accountId: String): String =
        "pixelfed/account/${Uri.encode(accountId)}"

    fun lemmyUserDetail(username: String): String =
        "lemmy/user/${Uri.encode(username)}"

    fun mastodonHashtagTimeline(hashtag: String): String =
        "search/hashtag/${Uri.encode(hashtag)}"

    fun mastodonMediaViewer(
        urls: List<String>,
        altFlags: List<Boolean>,
        initialIndex: Int,
    ): String {
        val encodedUrls = Uri.encode(urls.joinToString("|"))
        val encodedAltFlags = altFlags.joinToString(",") { if (it) "1" else "0" }
        return "mastodon/media?urls=$encodedUrls&alts=$encodedAltFlags&index=$initialIndex"
    }
}
