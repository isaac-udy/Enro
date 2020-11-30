package nav.enro.core

import kotlin.reflect.KClass

// This class is used primarily to simplify the lambda signature of NavigationExecutor.open
class ExecutorArgs<FromContext: Any, OpensContext: Any, KeyType: NavigationKey>(
    val fromContext: NavigationContext<out FromContext>,
    val navigator: Navigator<out KeyType, out OpensContext>,
    val key: KeyType,
    val instruction: NavigationInstruction.Open
)

// TODO add pre/post open for more configuration
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

