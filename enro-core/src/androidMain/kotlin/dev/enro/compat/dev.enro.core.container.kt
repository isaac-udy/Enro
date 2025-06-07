package dev.enro.core.container

import dev.enro.NavigationBackstack
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance
import dev.enro.ui.NavigationContainerState

public fun backstackOf(vararg elements: NavigationKey.Instance<out NavigationKey>): NavigationBackstack {
    return elements.toList()
}

public fun emptyBackstack(): NavigationBackstack = backstackOf()

public fun NavigationContainerState.setBackstack(backstack: NavigationBackstack) {
    setBackstack { backstack }
}

public fun NavigationContainerState.setBackstack(block: (NavigationBackstack) -> NavigationBackstack) {
    execute(
        operation = NavigationOperation.SetBackstack(
            currentBackstack = container.backstack,
            targetBackstack = block(container.backstack),
        )
    )
}

public fun NavigationBackstack.push(key: NavigationKey) : NavigationBackstack {
    return this + key.asInstance()
}