package dev.enro.core.result.flows

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey

public class FlowStepBuilderScope<T: Any> @PublishedApi internal constructor(
    private val builder: FlowStepBuilder<T>,
) {
    /**
     * Configure this step to be considered a "transient" step in the flow. This means that the step will be:
     * a) skipped when navigating back
     * b) skipped when navigating forward if the step already has a result, and the [dependsOn] values have not changed.
     *
     * This can be useful for displaying confirmation steps as part of the flow. For example, when a user completes a step of
     * the flow, you might want to confirm the user's action before proceeding to the next step. The confirmation step can
     * be marked as transient, and depend on the result of the previous step. This way, the user will be shown the confirmation
     * when they initially set the result, but will skip the confirmation when they navigate backwards through the flow, and
     * will also skip the confirmation when navigating forward if the result of the original step has not changed.
     *
     * Example:
     * Given a flow with three destinations, A, B, and C, where B is a transient step:
     * 1. When A returns a result, the user will be sent to B, and the backstack will be A -> B
     * 2. When B returns a result, the user will be sent to C, but the backstack will become A -> C
     * 3. When the user navigates back from C, they will be sent to A, skipping B
     * 4. When A returns a result for the second time, B may or may not be skipped, depending on whether or not it has a [dependsOn]
     *      a. If B has a [dependsOn] value, and the value has not changed, B will be skipped
     *      b. If B has a [dependsOn] value, and the value has changed, B will be shown
     *      c. If B does not have a [dependsOn] value, B will be skipped
     */
    public fun transient() {
        builder.addConfigurationItem(FlowStepConfiguration.Transient)
    }

    /**
     * Adds a dependency for this step being executed. This means that if the backstack of the navigation flow is manipulated,
     * this step will be re-executed if the dependencies have changed.
     *
     * Example:
     * Given a flow with destinations A, B, C and D, where no steps have any dependencies:
     * If the backstack for the flow is A -> B -> C -> D, and the user is moved back to A through manipulating the backstack,
     * after the user sets a result for A, both B and C will be skipped and the user will be moved back to D.
     *
     * Given a flow with destinations A, B, C and D, where B depends on the result of A:
     * If the backstack for the flow is A -> B -> C -> D, and the user is moved back to A through manipulating the backstack,
     * after the user sets a result for A, B will be re-executed, because it depends on the result of A, but C will be skipped
     * and the user will be moved back to D.
     */
    public fun dependsOn(vararg any: Any?) {
        builder.addDependencies(*any)
    }

    /**
     * Sets a default result for the step. This means that a result will be returned for this step when the user navigates to
     * this step for the first time, which means the step will be added to the backstack, but the user will skip over that step
     * and go directly to the next step. If the user then navigates back to this step, the step will not be skipped and they
     * will be able to interact with the screen that this step represents.
     *
     * This can be useful for pre-filling steps in a flow that is built from a form. For example, a user might be offered the
     * option to edit some form, where there may or may not be data available for some of the steps. The flow can be launched
     * with those steps pre-filled with the data that is available, but if the user was to navigate backwards through the flow,
     * or the backstack was manipulated to jump back to any of the previous steps, those steps would be available for editing.
     */
    public fun default(result: T) {
        builder.setDefault(result)
    }
}

public class FlowStepBuilder<T: Any> @PublishedApi internal constructor(
    private val dependencies: MutableList<Any?> = mutableListOf(),
    private var defaultValue: T? = null,
    private var configuration: MutableList<FlowStepConfiguration> = mutableListOf(),
) {
    @PublishedApi
    internal val scope: FlowStepBuilderScope<T> = FlowStepBuilderScope(this)

    internal fun addDependencies(vararg any: Any?) {
        dependencies.addAll(any.toList())
    }

    internal fun setDefault(result: T?) {
        defaultValue = result
    }

    internal fun addConfigurationItem(item: FlowStepConfiguration) {
        configuration.add(item)
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
        configuration = configuration.toSet(),
    )

    @PublishedApi
    internal fun build(
        stepId: String,
        navigationDirection: NavigationDirection,
        navigationKey: NavigationKey.WithExtras<out NavigationKey>,
    ) : FlowStep<T> = FlowStep(
        stepId = stepId,
        key = navigationKey,
        dependsOn = dependencies.toList(),
        direction = navigationDirection,
        configuration = configuration.toSet(),
    )

    @PublishedApi
    internal fun getDefaultResult(): T? = defaultValue

    internal fun copy(): FlowStepBuilder<T> {
        return FlowStepBuilder(
            dependencies = dependencies.toMutableList(),
            defaultValue = defaultValue,
            configuration = configuration.toMutableList(),
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