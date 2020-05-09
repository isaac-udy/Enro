package nav.enro.core.executors

import nav.enro.core.*
import nav.enro.core.context.NavigationContext
import nav.enro.core.navigator.Navigator
import kotlin.reflect.KClass
import kotlin.reflect.KType

// This class is used primarily to simplify the lambda signature of NavigationExecutor.open
class ExecutorArgs<FromContext: Any, OpensContext: Any, KeyType: NavigationKey>(
    val fromContext: NavigationContext<out FromContext, *>,
    val navigator: Navigator<out OpensContext, out KeyType>,
    val instruction: NavigationInstruction.Open<out KeyType>
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
        context: NavigationContext<out OpensContext, out KeyType>
    )
}

