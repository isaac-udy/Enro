package dev.enro.core.container

import androidx.activity.ComponentActivity
import dev.enro.core.*
import dev.enro.core.activity.ActivityNavigationContainer
import dev.enro.core.compatability.Compatibility

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

        when (instruction.navigationDirection) {
            NavigationDirection.ReplaceRoot,
            NavigationDirection.Present,
            NavigationDirection.Push -> {
                val containerManager = args.fromContext.containerManager

                val host = containerManager.activeContainer?.takeIf {
                    it.isVisible && it.accept(instruction)
                } ?: containerManager.containers
                    .filter { it.isVisible }
                    .firstOrNull { it.accept(instruction) }
                    .let {
                        val useActivityContainer = it == null &&
                                fromContext.parentContext() == null &&
                                instruction.navigationDirection != NavigationDirection.Push

                        when {
                            useActivityContainer -> ActivityNavigationContainer(fromContext.activity.navigationContext)
                            else -> it
                        }
                    }

                if (host == null) {
                    val parentContext = fromContext.parentContext()
                    if (parentContext == null) {
                        EnroException.MissingContainerForPushInstruction.logForStrictMode(
                            fromContext.controller,
                            args
                        )
                        open(
                            ExecutorArgs(
                                fromContext = fromContext,
                                binding = args.binding,
                                key = args.key,
                                instruction = args.instruction.internal.copy(
                                    navigationDirection = NavigationDirection.Present
                                )
                            )
                        )
                        if (isReplace) {
                            fromContext.getNavigationHandle().close()
                        }
                    } else {
                        open(
                            ExecutorArgs(
                                fromContext = parentContext,
                                binding = args.binding,
                                key = args.key,
                                instruction = args.instruction
                            )
                        )
                    }
                    return
                }

                EnroException.LegacyNavigationDirectionUsedInStrictMode.logForStrictMode(fromContext.controller, args)
                host.setBackstack { backstack ->
                    backstack
                        .let { if (isReplace) it.pop() else it }
                        .plus(instruction)
                }
            }
            else -> throw IllegalStateException()
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
}