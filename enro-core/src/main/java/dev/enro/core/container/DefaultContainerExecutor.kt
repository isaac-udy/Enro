package dev.enro.core.container

import androidx.activity.ComponentActivity
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.ExecutorArgs
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationExecutor
import dev.enro.core.NavigationKey
import dev.enro.core.activity
import dev.enro.core.activity.ActivityNavigationContainer
import dev.enro.core.compatability.Compatibility
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationContext
import dev.enro.core.parentContainer
import dev.enro.core.readOpenInstruction

internal object DefaultContainerExecutor : NavigationExecutor<Any, Any, NavigationKey>(
    fromType = Any::class,
    opensType = Any::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out Any, out NavigationKey>) {
        if (Compatibility.DefaultContainerExecutor.earlyExitForFragments(args)) return
        if (Compatibility.DefaultContainerExecutor.earlyExitForReplace(args)) return

        val isReplace = args.instruction.navigationDirection == NavigationDirection.Replace
        val fromContext = args.fromContext
        val instruction = Compatibility.DefaultContainerExecutor.getInstructionForCompatibility(args)

        val container = findContainerFor(fromContext, instruction)
        if (
            Compatibility.DefaultContainerExecutor.earlyExitForMissingContainerPush(
                fromContext = fromContext,
                instruction = instruction,
                container = container,
            )
        ) return

        requireNotNull(container) {
            "Failed to execute instruction from context with NavigationKey ${fromContext.arguments.readOpenInstruction()!!.navigationKey::class.java.simpleName}: Could not find valid container for NavigationKey of type ${keyType.java.simpleName}"
        }
        container.setBackstack { backstack ->
            backstack
                .let { if (isReplace) it.pop() else it }
                .plus(instruction)
        }
    }

    override fun close(context: NavigationContext<out Any>) {
        if (Compatibility.DefaultContainerExecutor.earlyExitForNoContainer(context)) return

        val container = context.parentContainer()
            ?: (context.contextReference as? ComponentActivity)?.let {
                ActivityNavigationContainer(context as NavigationContext<ComponentActivity>)
            }
            ?: return

        container.setBackstack {
            it.close(
                context.getNavigationHandle().id
            )
        }
    }

    private fun findContainerFor(
        fromContext: NavigationContext<*>?,
        instruction: AnyOpenInstruction,
    ): NavigationContainer? {
        if (fromContext == null) return null
        if (instruction.navigationDirection == NavigationDirection.ReplaceRoot) {
            return ActivityNavigationContainer(fromContext.activity.navigationContext)
        }
        val containerManager = fromContext.containerManager
        val defaultFragmentContainer = containerManager
            .containers
            .firstOrNull { it.key == NavigationContainerKey.FromId(android.R.id.content) }

        val container = containerManager.activeContainer?.takeIf {
            it.isVisible && it.accept(instruction) && it != defaultFragmentContainer
        } ?: containerManager.containers
            .filter { it.isVisible }
            .filterNot { it == defaultFragmentContainer }
            .firstOrNull { it.accept(instruction) }
            .let {
                val useDefaultFragmentContainer = it == null &&
                        fromContext.parentContext == null &&
                        defaultFragmentContainer != null &&
                        defaultFragmentContainer.accept(instruction)

                val useActivityContainer = it == null &&
                        fromContext.parentContext == null &&
                        instruction.navigationDirection != NavigationDirection.Push

                when {
                    useDefaultFragmentContainer -> defaultFragmentContainer
                    useActivityContainer -> ActivityNavigationContainer(fromContext.activity.navigationContext)
                    else -> it
                }
            }

        return container ?: findContainerFor(fromContext.parentContext, instruction)
    }
}