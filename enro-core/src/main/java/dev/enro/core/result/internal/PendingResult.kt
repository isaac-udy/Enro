package dev.enro.core.result.internal

import dev.enro.core.result.ResultChannelId
import kotlin.reflect.KClass

internal data class PendingResult(
    val resultChannelId: ResultChannelId,
    val resultType: KClass<out Any>,
    val result: Any
)
