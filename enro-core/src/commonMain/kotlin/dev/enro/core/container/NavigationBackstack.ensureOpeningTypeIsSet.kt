package dev.enro.core.container

import dev.enro.core.EnroException
import dev.enro.core.NavigationContext
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor

internal fun NavigationBackstack.ensureOpeningTypeIsSet(
    parentContext: NavigationContext<*>
): NavigationBackstack {
    return map {
        if (it.internal.openingType != null) return@map it

        InstructionOpenedByInterceptor.intercept(
            it,
            parentContext,
            parentContext.controller.bindingForInstruction(it)
                ?: throw EnroException.MissingNavigationBinding(it.navigationKey),
        )
    }.toBackstack()
}