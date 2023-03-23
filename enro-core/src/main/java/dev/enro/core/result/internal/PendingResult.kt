package dev.enro.core.result.internal

import kotlin.reflect.KClass

internal sealed class PendingResult {
    abstract val resultChannelId: ResultChannelId

    class Closed(
        override val resultChannelId: ResultChannelId
    ): PendingResult()

    data class Result(
        override val resultChannelId: ResultChannelId,
        val resultType: KClass<out Any>,
        val result: Any
    ) : PendingResult()

}
