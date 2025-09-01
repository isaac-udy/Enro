package dev.enro.result.flow

import dev.enro.NavigationKey
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

public abstract class FlowStepDefinition<T : NavigationKey, R : Any> @PublishedApi internal constructor() {
    public abstract val keyWithMetadata: NavigationKey.WithMetadata<T>
    public abstract val result: KClass<R>

    @PublishedApi
    internal var providedId: FlowStep.Id<T>? = null
    @PublishedApi
    internal var defaultResult: R? = null
    private val dependencies = mutableListOf<Any?>()
    private val configuration = mutableSetOf<FlowStepOptions>()

    @PublishedApi
    internal fun buildStep(
        navigationFlowScope: NavigationFlowScope,
    ): FlowStep<R> {
        val steps = navigationFlowScope.steps
        val id = when(val providedId = providedId)  {
            null -> {
                val baseId = this::class.qualifiedName ?: this::class.toString()
                val count = steps.count { it.id.value.startsWith(baseId) }
                FlowStep.Id("$baseId@$count")
            }
            else -> {
                require(steps.none { it.id == providedId }) {
                    "Step with id $providedId already exists in the flow."
                }
                providedId
            }
        }
        if (configuration.contains(FlowStepOptions.AlwaysAfterPrevious)) {
            steps.lastOrNull()?.let {
                val previousResult = navigationFlowScope.resultManager.getResultInstanceId(it.id)
                dependencies.add(previousResult)
            }
        }
        return FlowStep(
            id = id,
            key = keyWithMetadata,
            dependsOn = dependencies,
            options = configuration,
        )
    }

    public class ConfigurationScope<T : NavigationKey>(
        @PublishedApi internal val definition: FlowStepDefinition<T, *>,
    ) {
        public val key: T get() = definition.keyWithMetadata.key
        
        /**
         * Sets an exact FlowStep.Id for this flow step. FlowStep.Id instances must be unique within a flow,
         * and re-using a FlowStep.Id will result in an exception being thrown when the flow is built.
         *
         * Setting a FlowStep.Id is useful when you want to get a reference to a flow step using
         * [NavigationFlow.getStep] or [NavigationFlow.requireStep], to perform actions on the step,
         * such as [FlowStepReference.editStep] or [FlowStepReference.clearResult].
         *
         * FlowStep.Id instances can be created using the [flowStepId] function.
         *
         * Example:
         *
         * ```
         *
         * val firstStepId = flowStepId<FirstStepScreen>()
         * val secondStepId = flowStepId<SecondStepScreen>()
         *
         * val flow = registerForFlowResult {
         *     val firstResult = open(FirstStepScreen) {
         *         id(firstStepId)
         *     }
         *     val secondResult = open(SecondStepScreen) {
         *         id(secondStepId)
         *     }
         *     ...
         * }
         *
         * fun onEditFirstStep() {
         *     flow.getStep(firstStepId)?.editStep()
         * }
         *
         * fun onEditSecondStep() {
         *     flow.getStep(secondStepId)?.editStep()
         * }
         *
         * ```
         *
         */
        public fun id(id: FlowStep.Id<T>) {
            definition.providedId = id
        }

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
            definition.configuration.add(FlowStepOptions.Transient)
        }

        /**
         * Adds a dependency for this step. When a NavigationFlow updates the backstack, it will normally
         * skip steps that have already been completed. If a step has dependencies, the step will only be
         * skipped if the dependencies have not changed.
         *
         * Example:
         * Given a flow with destinations A, B, C and D, where no steps have any dependencies:
         * If the backstack for the flow is A -> B -> C -> D, and the user is moved back to A (for example, by
         * calling [FlowStepReference.editStep] or directly manipulating the backstack), after the user sets a result
         * for A, both B and C will be skipped and the user will be moved back to D.
         *
         * Given a flow with destinations A, B, C and D, where B depends on the result of A:
         * If the backstack for the flow is A -> B -> C -> D, and the user is moved back to A (for example, by
         * calling [FlowStepReference.editStep] or directly manipulating the backstack), after the user sets a result
         * for A, B will be re-executed if the result of A has changed, but once B is completed,
         * C will be skipped and the user will be moved to D.
         *
         * ```
         *    val firstResult = open(FirstNavigationKey())
         *    val secondResult = open(SecondNavigationKey()) {
         *         // if firstResult changes, get a new result from SecondNavigationKey()
         *         dependsOn(firstResult)
         *    }
         * ```
         *
         * If the NavigationKey for this step properly implements equals/hashCode, then it may be useful
         * to add a dependency on the NavigationKey itself, which will cause the step to be re-executed if the
         * NavigationKey changes.
         *
         * ```
         *     val firstResult = open(FirstNavigationKey())
         *
         *     // If data from firstResult is used to construct SecondNavigationKey,
         *     // it may be useful to add a dependsOn for "key"
         *     val secondResult = open(
         *         key = SecondNavigationKey(
         *             data = firstResult.data,
         *             otherData = firstResult.otherData,
         *         )
         *     ) {
         *         dependsOn(key)
         *     }
         * ```
         */
        public fun dependsOn(dependency: Any?) {
            definition.dependencies.add(dependency)
        }

        /**
         * alwaysAfterPreviousStep causes this step to run whenever the previous step is completed,
         * even if the result of the previous step has not changed.
         *
         * This is useful in NavigationFlows that use [FlowStepReference.editStep], where you want to ensure that a
         * step is always run after the previous step, even if the result of the previous step has not changed.
         *
         * An example of where this could be used is when a NavigationFlow branches based on the result of a step,
         * and you want to ensure that the branch is always run after the previous step, even if the result of the previous
         * step has not changed.
         *
         * In the example below, we run the "SelectRepaymentType" step after the "SelectLoanType" step,
         * even if the result of the "SelectLoanType" step has not changed. This means that if the user is
         * navigated back to the "SelectLoanType" step (for example, through [FlowStepReference.editStep]),
         * the user will always be presented with the "SelectRepaymentType" step, no matter what the result of the
         * "SelectLoanType" step is:
         *
         * Example:
         * ```
         *   val loanType = open(SelectLoanType())
         *   val repayments = open(SelectRepaymentType(loanType)) {
         *      alwaysAfterPreviousStep()
         *   }
         * ```
         *
         */
        public fun alwaysAfterPreviousStep() {
            definition.configuration.add(FlowStepOptions.AlwaysAfterPrevious)
        }
    }
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
 *
 * Defaults are only configured once per execution of a NavigationFlow
 */
public fun <T: NavigationKey> FlowStepDefinition.ConfigurationScope<T>.default() {
    @Suppress("UNCHECKED_CAST")
    definition as FlowStepDefinition<T, Unit>
    definition.defaultResult = Unit
}

@Suppress("UnusedReceiverParameter")
@JvmName("defaultWithoutResult")
@Deprecated(
    message = "default() is not supported for steps with a result type. Use default(result: R) instead.",
    level = DeprecationLevel.ERROR,
)
public fun <T: NavigationKey.WithResult<R>, R: Any> FlowStepDefinition.ConfigurationScope<T>.default() {
    error("default() is not supported for steps with a result type. Use default(result: R) instead.")
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
 *
 * Defaults are only configured once per execution of a NavigationFlow, and changing the value provided to this function
 * will not result in the defa
 */
public fun <T: NavigationKey.WithResult<R>, R: Any> FlowStepDefinition.ConfigurationScope<T>.default(result: R) {
    @Suppress("UNCHECKED_CAST")
    definition as FlowStepDefinition<T, R>
    definition.defaultResult = result
}