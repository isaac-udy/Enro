package dev.enro.core.container

import android.os.Parcelable
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
public value class NavigationBackstack(private val backstack: List<AnyOpenInstruction>) : List<AnyOpenInstruction> by backstack, Parcelable {
    public val active: AnyOpenInstruction? get() = lastOrNull()
}

public fun emptyBackstack() : NavigationBackstack = NavigationBackstack(emptyList())
public fun backstackOf(vararg instructions: AnyOpenInstruction) : NavigationBackstack = NavigationBackstack(instructions.toList())
public fun backstackOfNotNull(vararg instructions: AnyOpenInstruction?) : NavigationBackstack = NavigationBackstack(instructions.filterNotNull())

public fun List<AnyOpenInstruction>.toBackstack() : NavigationBackstack {
    if (this is NavigationBackstack) return this
    return NavigationBackstack(this)
}

internal fun NavigationBackstack.ensureOpeningTypeIsSet(
    parentContext: NavigationContext<*>
): NavigationBackstack {
    return map {
        if (it.internal.openingType != Any::class.java) return@map it

        InstructionOpenedByInterceptor.intercept(
            it,
            parentContext,
            requireNotNull(parentContext.controller.bindingForKeyType(it.navigationKey::class)),
        )
    }.toBackstack()
}

internal fun NavigationBackstack.close(): NavigationBackstack {
    return dropLast(1).toBackstack()
}

internal fun NavigationBackstack.close(id: String): NavigationBackstack {
    val index = indexOfLast {
        it.instructionId == id
    }
    if (index < 0) return this
    val exiting = get(index)
    return minus(exiting).toBackstack()
}