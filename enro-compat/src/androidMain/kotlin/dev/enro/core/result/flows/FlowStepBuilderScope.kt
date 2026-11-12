package dev.enro.core.result.flows

import dev.enro.result.flow.FlowStepOptions

@Deprecated("This class is no longer used and will be removed in a future release.")
public class FlowStepBuilderScope<T : Any> @PublishedApi internal constructor() {
    @PublishedApi
    internal var defaultResult: T? = null
    @PublishedApi
    internal val dependencies: MutableList<Any?> = mutableListOf()
    @PublishedApi
    internal val configuration: MutableSet<FlowStepOptions> = mutableSetOf()

    /**
     * Marks the step as transient. A transient step is:
     * a) skipped when the user navigates back
     * b) skipped when the user navigates forward, if the step has a result and the [dependsOn] values have not changed.
     *
     * Example: a flow with three destinations, A, B and C, where B is a transient step (such as a confirmation of A):
     * 1. A returns a result: B is opened, and the backstack is A -> B
     * 2. B returns a result: C is opened, and the backstack is A -> C
     * 3. The user navigates back from C: A is shown, and B is skipped
     * 4. A returns a result for the second time:
     *      a. B has a [dependsOn] value that has not changed: B is skipped
     *      b. B has a [dependsOn] value that has changed: B is shown
     *      c. B has no [dependsOn] value: B is skipped
     */
    public fun transient() {
        configuration.add(FlowStepOptions.Transient)
    }

    /**
     * Adds dependencies to the step. When the backstack of the navigation flow is changed, the step is executed again
     * if a dependency of the step has changed.
     *
     * Example: a flow with destinations A, B, C and D has the backstack A -> B -> C -> D. The backstack is changed
     * to A, and the user sets a result for A.
     * - No step has a dependency: B and C are skipped, and D is shown.
     * - B depends on the result of A: B is executed again, C is skipped, and D is shown after B.
     */
    public fun dependsOn(vararg any: Any?) {
        any.forEach {
            dependencies.add(it)
        }
    }

    /**
     * Sets a default result for the step. The first time the flow executes the step, the step is added to the backstack
     * with [result] and the flow continues to the next step without showing the step. When the user navigates back to the
     * step, the step is shown and the user can change the result.
     *
     * Example: a flow that edits a form sets the existing form data as the default result of each step.
     */
    public fun default(result: T) {
        defaultResult = result
    }
}