package dev.enro.core.container

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor

public fun createEmptyBackStack(): NavigationBackstackState = NavigationBackstackState(
    lastInstruction = NavigationInstruction.Close,
    backstack = emptyList(),
    exiting = null,
    exitingIndex = -1,
    updateType = NavigationBackstackState.UpdateType.INITIAL_STATE
)

public fun createRootBackStack(rootInstruction: AnyOpenInstruction?): NavigationBackstackState =
    NavigationBackstackState(
        lastInstruction = NavigationInstruction.Close,
        backstack = listOfNotNull(rootInstruction),
        exiting = null,
        exitingIndex = -1,
        updateType = NavigationBackstackState.UpdateType.INITIAL_STATE
    )

public fun createRootBackStack(backstack: List<AnyOpenInstruction>): NavigationBackstackState =
    NavigationBackstackState(
        lastInstruction = backstack.lastOrNull() ?: NavigationInstruction.Close,
        backstack = backstack,
        exiting = null,
        exitingIndex = -1,
        updateType = NavigationBackstackState.UpdateType.INITIAL_STATE
    )

public fun createRestoredBackStack(backstack: List<AnyOpenInstruction>): NavigationBackstackState =
    NavigationBackstackState(
        backstack = backstack,
        exiting = null,
        exitingIndex = -1,
        lastInstruction = backstack.lastOrNull() ?: NavigationInstruction.Close,
        updateType = NavigationBackstackState.UpdateType.RESTORED_STATE
    )

@Stable
@Immutable
public data class NavigationBackstackState(
    val lastInstruction: NavigationInstruction,
    val backstack: List<AnyOpenInstruction>,
    val exiting: AnyOpenInstruction?,
    val exitingIndex: Int,
    val updateType: UpdateType
) {
    val active: AnyOpenInstruction? = backstack.lastOrNull()

    val renderable: List<AnyOpenInstruction> = run {
        if (exiting == null) return@run backstack
        if (backstack.contains(exiting)) return@run backstack
        if (exitingIndex > backstack.lastIndex) return@run backstack + exiting
        return@run backstack.flatMapIndexed { index, open ->
            if (exitingIndex == index) return@flatMapIndexed listOf(exiting, open)
            return@flatMapIndexed listOf(open)
        }
    }

    public val isRestoredState: Boolean get() = updateType == UpdateType.RESTORED_STATE
    public val isInitialState: Boolean get() = updateType == UpdateType.INITIAL_STATE

    public enum class UpdateType {
        RESTORED_STATE,
        INITIAL_STATE,
        STANDARD;
    }

    public companion object {
        public val Saver: Saver<NavigationBackstackState, List<AnyOpenInstruction>> = Saver(
            save = { it.backstack },
            restore = {
                createRestoredBackStack(it)
            }
        )
        public val MutableSaver: Saver<MutableState<NavigationBackstackState>, List<AnyOpenInstruction>> = Saver(
            save = { it.value.backstack },
            restore = {
                mutableStateOf(createRestoredBackStack(it))
            }
        )
    }
}

internal fun NavigationBackstackState.add(
    vararg instructions: AnyOpenInstruction
): NavigationBackstackState {
    if (instructions.isEmpty()) return this
    return copy(
        backstack = backstack + instructions,
        exiting = active,
        exitingIndex = backstack.lastIndex,
        lastInstruction = instructions.last(),
        updateType = NavigationBackstackState.UpdateType.STANDARD
    )
}

internal fun NavigationBackstackState.close(): NavigationBackstackState {
    return copy(
        backstack = backstack.dropLast(1),
        exiting = backstack.lastOrNull(),
        exitingIndex = backstack.lastIndex,
        lastInstruction = NavigationInstruction.Close,
        updateType = NavigationBackstackState.UpdateType.STANDARD
    )
}

internal fun NavigationBackstackState.close(id: String): NavigationBackstackState {
    val index = backstack.indexOfLast {
        it.instructionId == id
    }
    if (index < 0) return this
    val exiting = backstack[index]
    return copy(
        backstack = backstack.minus(exiting),
        exiting = exiting,
        exitingIndex = index,
        lastInstruction = NavigationInstruction.Close,
        updateType = NavigationBackstackState.UpdateType.STANDARD
    )
}

internal fun NavigationBackstackState.ensureOpeningTypeIsSet(
    parentContext: NavigationContext<*>
): NavigationBackstackState {
    return copy(
        backstack = backstack.map {
            if (it.internal.openingType != Any::class.java) return@map it

            InstructionOpenedByInterceptor.intercept(
                it,
                parentContext,
                requireNotNull(parentContext.controller.bindingForKeyType(it.navigationKey::class)),
            )
        },
        lastInstruction = lastInstruction.let {
            if (it !is AnyOpenInstruction) return@let it
            if (it.internal.openingType != Any::class.java) return@let it

            InstructionOpenedByInterceptor.intercept(
                it,
                parentContext,
                requireNotNull(parentContext.controller.bindingForKeyType(it.navigationKey::class)),
            )
        },
        exiting = exiting?.let {
            if (it.internal.openingType != Any::class.java) return@let it

            InstructionOpenedByInterceptor.intercept(
                it,
                parentContext,
                requireNotNull(parentContext.controller.bindingForKeyType(it.navigationKey::class)),
            )
        }
    )
}