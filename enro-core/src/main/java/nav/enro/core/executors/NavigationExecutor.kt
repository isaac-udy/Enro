package nav.enro.core.executors

import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.context.NavigationContext
import nav.enro.core.navigator.Navigator
import kotlin.reflect.KClass

// This class is used primarily to simplify the lambda signature of NavigationExecutor.open
class ExecutorArgs<FromContext: Any, OpensContext: Any, KeyType: NavigationKey>(
    val fromContext: NavigationContext<out FromContext>,
    val navigator: Navigator<out OpensContext, out KeyType>,
    val key: KeyType,
    val instruction: NavigationInstruction.Open
)


abstract class NavigationExecutor<FromContext: Any, OpensContext: Any, KeyType: NavigationKey>(
    val fromType: KClass<FromContext>,
    val opensType: KClass<OpensContext>,
    val keyType: KClass<KeyType>
) {
    abstract fun open(
        args: ExecutorArgs<out FromContext, out OpensContext, out KeyType>
    )

    abstract fun close(
        context: NavigationContext<out OpensContext>
    )
}

