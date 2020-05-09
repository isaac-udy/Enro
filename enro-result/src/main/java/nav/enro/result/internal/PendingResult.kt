package nav.enro.result.internal

import kotlin.reflect.KClass

internal data class PendingResult(
    val resultChannelId: ResultChannelId,
    val resultType: KClass<out Any>,
    val result: Any
)
