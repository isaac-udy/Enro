import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.EnroException
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.GetNavigationBinding
import kotlin.reflect.KClass

/**
 * A NavigationHostFactory allows for destinations of different types to be interoperable with each other. For example,
 * a Fragment destination can host a Composable destination. There are two important functions to register here:
 * - supports: This function should return true if the NavigationHostFactory can host the provided NavigationInstruction.Open
 * - wrap: This function should return a new NavigationInstruction.Open that is compatible with the HostType
 */
@AdvancedEnroApi
public abstract class NavigationHostFactory<HostType: Any>(
    public val hostType: KClass<HostType>,
) {
    internal lateinit var dependencyScope: EnroDependencyScope

    private val getNavigationBinding: GetNavigationBinding by lazy { dependencyScope.get() }

    protected fun getNavigationBinding(instruction: NavigationInstruction.Open<*>): NavigationBinding<*, *>?
        = getNavigationBinding.invoke(instruction)

    protected fun requireNavigationBinding(instruction: NavigationInstruction.Open<*>): NavigationBinding<*, *>
            = getNavigationBinding.require(instruction)

    protected fun cannotCreateHost(instruction: NavigationInstruction.Open<*>): Nothing {
        throw EnroException.CannotCreateHostForType(hostType, instruction.internal.openingType)
    }

    public abstract fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): Boolean

    public abstract fun wrap(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): NavigationInstruction.Open<*>
}