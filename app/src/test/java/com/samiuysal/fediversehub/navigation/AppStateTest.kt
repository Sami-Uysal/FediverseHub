package com.samiuysal.fediversehub.navigation

import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppStateTest {
    @Test
    fun selectedAccount_prefersActiveAccountForSelectedPlatform() {
        val mastodon = account("m1", PlatformType.MASTODON)
        val pixelfed = account("p1", PlatformType.PIXELFED)

        val state = AppState(
            selectedPlatform = PlatformType.PIXELFED,
            accounts = listOf(mastodon, pixelfed),
            activeAccountIds = mapOf(PlatformType.PIXELFED to pixelfed.id),
        )

        assertEquals(pixelfed, state.selectedAccount)
    }

    @Test
    fun selectedAccount_fallsBackToFirstPlatformAccount() {
        val account = account("l1", PlatformType.LEMMY)

        val state = AppState(
            selectedPlatform = PlatformType.LEMMY,
            accounts = listOf(account),
            activeAccountIds = mapOf(PlatformType.LEMMY to "missing"),
        )

        assertEquals(account, state.selectedAccount)
    }

    @Test
    fun selectedAccount_returnsNullWhenPlatformHasNoAccount() {
        val state = AppState(
            selectedPlatform = PlatformType.PIXELFED,
            accounts = listOf(account("m1", PlatformType.MASTODON)),
        )

        assertNull(state.selectedAccount)
    }

    private fun account(id: String, platform: PlatformType): Account =
        Account(
            id = id,
            platform = platform,
            instanceUrl = "example.social",
            username = id,
            displayName = id,
            avatarUrl = null,
            accessToken = "token",
        )
}
