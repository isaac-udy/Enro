package dev.enro.core.container

import android.app.Activity
import dev.enro.core.*
import dev.enro.core.activity.ActivityNavigationContainer
import dev.enro.core.compatability.earlyExitForFragments
import dev.enro.core.compatability.earlyExitForReplace
import dev.enro.core.compatability.getInstructionForCompatibility
import dev.enro.core.compatability.handleCloseWithNoContainer
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.controller.usecase.HostInstructionAs

internal object DefaultContainerExecutor : NavigationExecutor<Any, Any, NavigationKey>(
    fromType = Any::class,
    opensType = Any::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out Any, out NavigationKey>) {
        if (args.earlyExitForFragments()) return
        if (args.earlyExitForReplace()) return

        val isReplace = args.instruction.navigationDirection == NavigationDirection.Replace
        val fromContext = args.fromContext
        val instruction = args.getInstructionForCompatibility()

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
                        val useActivityContainer = it == null && fromContext.parentContext() == null
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

                        if (instruction.navigationDirection == NavigationDirection.Present) {
                            openInstructionAsActivity(
                                fromContext,
                                NavigationDirection.Present,
                                instruction
                            )
                        } else {
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
                        }
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
        if (handleCloseWithNoContainer(context)) return

        val container = context.parentContainer() ?: return
        container.setBackstack(
            container.backstackFlow.value.close(
                context.getNavigationHandle().id
            )
        )
    }
}

private fun openInstructionAsActivity(
    fromContext: NavigationContext<out Any>,
    navigationDirection: NavigationDirection,
    instruction: AnyOpenInstruction
) {
    val open = fromContext.controller.dependencyScope.get<ExecuteOpenInstruction>()
    val hostInstructionAs = fromContext.controller.dependencyScope.get<HostInstructionAs>()

    open.invoke(
        fromContext,
        hostInstructionAs<Activity>(
            fromContext,
            instruction.asDirection(navigationDirection)
        ),
    )
}