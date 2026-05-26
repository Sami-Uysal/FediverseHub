package com.samiuysal.fediversehub.feature.lemmy.mapper

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LemmyApiMapperTest {
    @Test
    fun isLemmySubscribed_treatsPendingSubscriptionAsSubscribedState() {
        assertTrue("Pending".isLemmySubscribed())
    }

    @Test
    fun isLemmySubscribed_treatsNotSubscribedAsFalse() {
        assertFalse("NotSubscribed".isLemmySubscribed())
    }

    @Test
    fun isLemmySubscribed_treatsMissingValueAsFalse() {
        assertFalse(null.isLemmySubscribed())
    }
}
