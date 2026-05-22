package com.samiuysal.fediversehub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import com.samiuysal.fediversehub.core.designsystem.component.AppBottomNavItem

object AppDestination {
    const val HOME = "home"
    const val SEARCH = "search"
    const val CREATE = "create"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val AUTH_MASTODON = "auth/mastodon"

    val bottomNavItems = listOf(
        AppBottomNavItem(HOME, "Home", Icons.Outlined.Home),
        AppBottomNavItem(SEARCH, "Search", Icons.Outlined.Search),
        AppBottomNavItem(CREATE, "Discover", Icons.Outlined.Explore),
        AppBottomNavItem(NOTIFICATIONS, "Notifications", Icons.Outlined.Notifications),
        AppBottomNavItem(PROFILE, "Profile", Icons.Outlined.Person),
    )
}
