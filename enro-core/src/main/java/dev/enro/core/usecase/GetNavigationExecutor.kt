package dev.enro.core.usecase

import dev.enro.core.*

internal interface GetNavigationExecutor {
    operator fun invoke(types: Pair<Class<out Any>, Class<out Any>>): NavigationExecutor<Any, Any, NavigationKey>
}

internal fun GetNavigationExecutor.forOpening(instruction: AnyOpenInstruction) =
    invoke(instruction.internal.openedByType to instruction.internal.openingType)

internal fun GetNavigationExecutor.forClosing(navigationContext: NavigationContext<*>) =
    invoke(navigationContext.getNavigationHandle().instruction.internal.openedByType to navigationContext.contextReference::class.java)