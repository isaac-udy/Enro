package dev.enro.result.flow

import dev.enro.NavigationKey
import dev.enro.platform.callSiteClassName
import dev.enro.stableClassName
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
                val baseId = callSiteClassName(this::class) ?: stableClassName(keyWithMetadata.key::class)
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
         * Sets the [FlowStep.Id] of this step. Each [FlowStep.Id] must be unique within a flow:
         * a flow that declares two steps with the same [FlowStep.Id] throws
         * [IllegalArgumentException] when the flow builds the second step.
         *
         * [NavigationFlow.getStep] and [NavigationFlow.requireStep] take a [FlowStep.Id] and return
         * a [FlowStepReference]. Create a [FlowStep.Id] with [flowStepId].
         *
         * ```
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
         * ```
         */
        public fun id(id: FlowStep.Id<T>) {
            definition.providedId = id
        }

        /**
         * Marks this step as transient. The flow removes a transient step from the backstack when
         * a later step becomes the active step. The flow skips a transient step:
         * - when the user navigates back
         * - when the user navigates forward, the transient step already has a result, and the
         *   [dependsOn] values of the transient step are unchanged
         *
         * Example: a flow with destinations A, B and C, where B is a transient step that confirms
         * the result of A:
         * 1. A returns a result. The backstack is A -> B.
         * 2. B returns a result. The backstack is A -> C.
         * 3. The user navigates back from C. The backstack is A.
         * 4. A returns a result again. The flow shows B only if B has a [dependsOn] value and the
         *    [dependsOn] value changed. Otherwise the backstack is A -> C.
         */
        public fun transient() {
            definition.configuration.add(FlowStepOptions.Transient)
        }

        /**
         * Adds a dependency to this step. A [NavigationFlow] skips a step that already has a
         * result. A step with dependencies is skipped only while each dependency is unchanged:
         * when a dependency changes, the flow discards the result of the step and shows the step
         * again.
         *
         * Example: a flow with destinations A, B, C and D and the backstack A -> B -> C -> D. The
         * user returns to A (for example, through [FlowStepReference.editStep]) and A returns a
         * result:
         * - With no dependencies, the flow skips B and C and shows D.
         * - With B depending on the result of A, the flow shows B if the result of A changed.
         *   After B returns a result, the flow skips C and shows D.
         *
         * ```
         *    val firstResult = open(FirstNavigationKey())
         *    val secondResult = open(SecondNavigationKey()) {
         *         // a changed firstResult discards the result of SecondNavigationKey()
         *         dependsOn(firstResult)
         *    }
         * ```
         *
         * A step whose [NavigationKey] implements `equals` and `hashCode` can depend on [key].
         * The flow then shows the step again when the [NavigationKey] changes.
         *
         * ```
         *     val firstResult = open(FirstNavigationKey())
         *
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
         * Shows this step each time the previous step returns a result, including a result equal
         * to the earlier result of the previous step.
         *
         * In the example, a user who returns to `SelectLoanType` (for example, through
         * [FlowStepReference.editStep]) and selects the same loan type is shown
         * `SelectRepaymentType` again:
         *
         * ```
         *   val loanType = open(SelectLoanType())
         *   val repayments = open(SelectRepaymentType(loanType)) {
         *      alwaysAfterPreviousStep()
         *   }
         * ```
         */
        public fun alwaysAfterPreviousStep() {
            definition.configuration.add(FlowStepOptions.AlwaysAfterPrevious)
        }
    }
}


/**
 * Marks the step as completed the first time the flow builds the step. The flow adds the step
 * to the backstack and shows the next step. A user who navigates back to the step can interact
 * with the step.
 *
 * The flow applies the default once for each step: a step whose result was cleared does not
 * receive the default again.
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
 * Sets [result] as the result of the step the first time the flow builds the step. The flow
 * adds the step to the backstack and shows the next step. A user who navigates back to the step
 * can interact with the step and return a different result.
 *
 * The flow applies the default once for each step: a later call with a different [result] does
 * not replace the result of the step, and a step whose result was cleared does not receive the
 * default again.
 */
public fun <T: NavigationKey.WithResult<R>, R: Any> FlowStepDefinition.ConfigurationScope<T>.default(result: R) {
    @Suppress("UNCHECKED_CAST")
    definition as FlowStepDefinition<T, R>
    definition.defaultResult = result
}