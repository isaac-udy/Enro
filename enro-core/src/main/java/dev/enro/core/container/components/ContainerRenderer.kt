package dev.enro.core.container.components

public interface ContainerRenderer {
    public val isVisible: Boolean
}

/*
    private val backstackUpdateJob = context.lifecycleOwner.lifecycleScope.launch {
        context.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (container.state.currentTransition === ContainerState.initialTransition) return@repeatOnLifecycle
            onBackstackUpdated(NavigationBackstackTransition(ContainerState.initialBackstack to container.backstack))
        }
    }

    public fun bind(
        context: NavigationContext<*>,
        state: ContainerState
    ) {
        context.lifecycleOwner.lifecycleScope.launch {
            context.lifecycle.withCreated {}
            state.backstackFlow.collectLatest {
                val transition = NavigationBackstackTransition(lastRenderedBackstack to it)
                while (!onBackstackUpdated(transition) && isActive) {
                    delay(16)
                }
                lastRenderedBackstack = it
            }
        }
    }
 */