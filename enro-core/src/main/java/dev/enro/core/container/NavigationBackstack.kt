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


internal fun merge(
    oldBackstack: List<AnyOpenInstruction>,
    newBackstack: List<AnyOpenInstruction>,
): List<AnyOpenInstruction> {
    val results = mutableMapOf<Int, MutableList<AnyOpenInstruction>>()
    val indexes = mutableMapOf<AnyOpenInstruction, Int>()
    val addedInstructions = newBackstack.map { it.instructionId }.toSet()
    newBackstack.forEachIndexed { index, it ->
        results[index] = mutableListOf(it)
        indexes[it] = index
    }
    results[-1] = mutableListOf()

    var oldIndex = -1
    oldBackstack.forEach { oldItem ->
        if(addedInstructions.contains(oldItem.instructionId)) return@forEach
        oldIndex = maxOf(indexes[oldItem] ?: -1, oldIndex)
        results[oldIndex].let {
            if(it == null) return@let
            if(it.firstOrNull() == oldItem) return@let
            it.add(oldItem)
        }
    }

    return results.entries
        .sortedBy { it.key }
        .flatMap { it.value }
}