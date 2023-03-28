package dev.enro.core.result.internal

import dev.enro.core.NavigationKey
import kotlin.reflect.KClass

internal sealed class PendingResult {
    abstract val resultChannelId: ResultChannelId
    abstract val navigationKey: NavigationKey.WithResult<*>

    class Closed(
        override val resultChannelId: ResultChannelId,
        override val navigationKey: NavigationKey.WithResult<*>,
    ): PendingResult()

    data class Result(
        override val resultChannelId: ResultChannelId,
        override val navigationKey: NavigationKey.WithResult<*>,
        val resultType: KClass<out Any>,
        val result: Any
    ) : PendingResult()

    internal companion object {
        const val OVERRIDE_NAVIGATION_KEY_EXTRA = "PendingResult.OVERRIDE_NAVIGATION_KEY_EXTRA"
    }
}
