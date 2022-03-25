package dev.enro.core.activity.container

import dev.enro.core.*
import dev.enro.core.ActivityContext
import dev.enro.core.container.NavigationContainerBackstack
import dev.enro.core.container.createEmptyBackStack
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ActivityNavigationContainer internal constructor(
    private val activityContext: ActivityContext<*>,
) : NavigationContainer {
    override val id: String = activityContext::class.java.name
    override val parentContext: NavigationContext<*> = activityContext
    override val emptyBehavior: EmptyBehavior = EmptyBehavior.CloseParent
    override val accept: (NavigationKey) -> Boolean = { true }
    override val activeContext: NavigationContext<*> = activityContext.leafContext()

    override val backstackFlow: StateFlow<NavigationContainerBackstack> = MutableStateFlow(
        createEmptyBackStack().push(
            activityContext.getNavigationHandleViewModel().instruction,
            null
        )
    )

    override fun setBackstack(backstack: NavigationContainerBackstack) {
        TODO("Not yet implemented")
    }
}