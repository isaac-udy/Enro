package nav.enro.core.internal.executors

import android.content.Intent
import nav.enro.core.*
import nav.enro.core.internal.context.NavigationContext
import nav.enro.core.internal.context.requireActivity
import kotlin.reflect.KClass

abstract class NavigationExecutor<FromContext: Any, OpensContext: Any, KeyType: NavigationKey>(
    val fromType: KClass<FromContext>,
    val opensType: KClass<OpensContext>,
    val keyType: KClass<KeyType>
) {
    abstract fun open(
        fromContext: NavigationContext<out FromContext, *>,
        navigator: Navigator<out OpensContext, out KeyType>,
        instruction: NavigationInstruction.Open<out KeyType>
    )

    abstract fun close(
        context: NavigationContext<out OpensContext, out KeyType>
    )
}

