package dev.enro.core.container

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor

@JvmInline
public value class NavigationBackstack(private val backstack: List<AnyOpenInstruction>) : List<AnyOpenInstruction> by backstack {
    public val active: AnyOpenInstruction? get() = lastOrNull()
}

public fun List<AnyOpenInstruction>.asBackstack() : NavigationBackstack = NavigationBackstack(this)

internal fun List<AnyOpenInstruction>.ensureOpeningTypeIsSet(
    parentContext: NavigationContext<*>
): List<AnyOpenInstruction> {
    return map {
        if (it.internal.openingType != Any::class.java) return@map it

        InstructionOpenedByInterceptor.intercept(
            it,
            parentContext,
            requireNotNull(parentContext.controller.bindingForKeyType(it.navigationKey::class)),
        )
    }
}

internal fun List<AnyOpenInstruction>.close(): List<AnyOpenInstruction> {
    return dropLast(1)
}

internal fun List<AnyOpenInstruction>.close(id: String): List<AnyOpenInstruction> {
    val index = indexOfLast {
        it.instructionId == id
    }
    if (index < 0) return this
    val exiting = get(index)
    return minus(exiting)
}