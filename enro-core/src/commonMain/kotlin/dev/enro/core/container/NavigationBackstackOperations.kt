package dev.enro.core.container

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey

public fun NavigationContainerContext.setBackstack(
    block: (NavigationBackstack) -> List<AnyOpenInstruction>
) {
    setBackstack(block(backstack).toBackstack())
}

public fun NavigationBackstack.close(matching: (NavigationKey) -> Boolean): NavigationBackstack {
    val instruction = lastOrNull {
        matching(it.navigationKey)
    } ?: return this

    return close(instruction.instructionId)
}

public fun NavigationBackstack.close(id: String): NavigationBackstack {
    val index = indexOfLast {
        it.instructionId == id
    }
    if (index < 0) return this
    return filterIndexed { i, _ -> i != index }.toBackstack()
}

public fun NavigationBackstack.close(instruction: AnyOpenInstruction): NavigationBackstack {
    return close(instruction.instructionId)
}

public fun NavigationBackstack.pop(): NavigationBackstack {
    return dropLast(1).toBackstack()
}

public fun NavigationBackstack.push(key: NavigationKey.SupportsPush): NavigationBackstack {
    return plus(NavigationInstruction.Push(key)).toBackstack()
}

public fun NavigationBackstack.present(key: NavigationKey.SupportsPresent): NavigationBackstack {
    return plus(NavigationInstruction.Present(key)).toBackstack()
}