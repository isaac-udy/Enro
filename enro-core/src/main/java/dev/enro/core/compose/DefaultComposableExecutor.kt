package dev.enro.core.compose

import androidx.compose.material.ExperimentalMaterialApi
import dev.enro.core.*
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.ComposeDialogFragmentHostKey
import dev.enro.core.compose.dialog.DialogDestination

object DefaultComposableExecutor : NavigationExecutor<Any, ComposableDestination, NavigationKey>(
    fromType = Any::class,
    opensType = ComposableDestination::class,
    keyType = NavigationKey::class
) {
    @OptIn(ExperimentalMaterialApi::class)
    override fun open(args: ExecutorArgs<out Any, out ComposableDestination, out NavigationKey>) {
        val containerManager = args.fromContext.containerManager
        val host = containerManager.activeContainer?.takeIf { it.accept(args.key) }
            ?: args.fromContext.containerManager.containers
                .filterIsInstance<ComposableNavigationContainer>()
                .firstOrNull { it.accept(args.key) }

        val isDialog = DialogDestination::class.java.isAssignableFrom(args.navigator.contextType.java)
                || BottomSheetDestination::class.java.isAssignableFrom(args.navigator.contextType.java)

        if(isDialog) {
            args.fromContext.controller.open(
                args.fromContext,
                NavigationInstruction.Open.OpenInternal(
                    args.instruction.navigationDirection,
                    ComposeDialogFragmentHostKey(args.instruction)
                )
            )
            return
        }

        if(host == null || args.instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            args.fromContext.controller.open(
                args.fromContext,
                NavigationInstruction.Open.OpenInternal(
                    args.instruction.navigationDirection,
                    ComposeFragmentHostKey(args.instruction)
                )
            )
            return
        }

        host.setBackstack(
            host.backstackFlow.value.push(args.instruction, args.fromContext.containerManager.activeContainer?.id)
        )
    }

    override fun close(context: NavigationContext<out ComposableDestination>) {
        val container = context.contextReference.contextReference.requireParentContainer()
        container.setBackstack(container.backstackFlow.value.close())
    }
}