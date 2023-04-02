package dev.enro.core.result.flows

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey

public class FlowStepBuilderScope<T: Any> @PublishedApi internal constructor(
    private val builder: FlowStepBuilder<T>
) {
    public fun dependsOn(vararg any: Any?) {
        builder.addDependencies(*any)
    }

    public fun default(result: T) {
        builder.setDefault(result)
    }
}

public class FlowStepBuilder<T: Any> @PublishedApi internal constructor(
    private val dependencies: MutableList<Any?> = mutableListOf(),
    private var defaultValue: T? = null,
) {
    @PublishedApi
    internal val scope: FlowStepBuilderScope<T> = FlowStepBuilderScope(this)

    internal fun addDependencies(vararg any: Any?) {
        dependencies.addAll(any.toList())
    }

    internal fun setDefault(result: T?) {
        defaultValue = result
    }

    @PublishedApi
    internal fun build(
        stepId: String,
        navigationDirection: NavigationDirection,
        navigationKey: NavigationKey,
    ) : FlowStep<T> = FlowStep(
        stepId = stepId,
        key = navigationKey,
        dependsOn = dependencies.toList(),
        direction = navigationDirection,
    )

    @PublishedApi
    internal fun getDefaultResult(): T? = defaultValue

    internal fun copy(): FlowStepBuilder<T> {
        return FlowStepBuilder(
            dependencies = dependencies.toMutableList(),
            defaultValue = defaultValue
        )
    }
}

public fun <T :Any> FlowStepBuilder<T>.withDefault(result: T): FlowStepBuilder<T> {
    return copy().apply {
        setDefault(result)
    }
}

public fun <T :Any> FlowStepBuilder<T>.withDependency(any: Any?): FlowStepBuilder<T> {
    return copy().apply {
        addDependencies(any)
    }
}