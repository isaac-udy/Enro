package dev.enro.core.result.internal

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationKey
import kotlin.reflect.KClass

internal sealed class PendingResult {
    abstract val resultChannelId: ResultChannelId
    abstract val instruction: AnyOpenInstruction
    abstract val navigationKey: NavigationKey.WithResult<*>

    class Closed(
        override val resultChannelId: ResultChannelId,
        override val instruction: AnyOpenInstruction,
       override val navigationKey:  NavigationKey.WithResult<*>,
    ) : PendingResult()

    data class Result(
        override val resultChannelId: ResultChannelId,
        override val instruction: AnyOpenInstruction,
        override val navigationKey: NavigationKey.WithResult<*>,
        val resultType: KClass<out Any>,
        val result: Any
    ) : PendingResult()
}
