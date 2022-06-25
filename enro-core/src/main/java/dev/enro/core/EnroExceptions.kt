package dev.enro.core

import android.util.Log
import dev.enro.core.controller.NavigationController

abstract class EnroException(
    private val inputMessage: String, cause: Throwable? = null
) : RuntimeException(cause) {
    override val message: String?
        get() = "${inputMessage.trim().removeSuffix(".")}. See https://github.com/isaac-udy/Enro/blob/main/docs/troubleshooting.md#${this::class.java.simpleName} for troubleshooting help"

    class NoAttachedNavigationHandle(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class CouldNotCreateEnroViewModel(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class ViewModelCouldNotGetNavigationHandle(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class MissingNavigator(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class IncorrectlyTypedNavigationHandle(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class InvalidViewForNavigationHandle(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class DestinationIsNotDialogDestination(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class EnroResultIsNotInstalled(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class ResultChannelIsNotInitialised(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class ReceivedIncorrectlyTypedResult(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class NavigationControllerIsNotAttached(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class NavigationContainerWrongThread(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class LegacyNavigationDirectionUsedInStrictMode(message: String, cause: Throwable? = null) : EnroException(message, cause) {
        companion object {
            fun logForStrictMode(navigationController: NavigationController, args: ExecutorArgs<*,*,*>) {
                when(args.instruction.navigationDirection) {
                    NavigationDirection.Present,
                    NavigationDirection.Push,
                    NavigationDirection.ReplaceRoot -> return
                    else -> { /* continue */ }
                }

                val message = "Opened ${args.key::class.java.simpleName} as a ${args.instruction.navigationDirection::class.java.simpleName} instruction. Forward and Replace type instructions are deprecated, please replace these with Push and Present instructions."
                if(navigationController.isStrictMode) {
                    throw LegacyNavigationDirectionUsedInStrictMode(message)
                }
                else {
                    Log.w("Enro", "$message Enro would have thrown in strict mode.")
                }
            }
        }
    }

    class MissingContainerForPushInstruction(message: String, cause: Throwable? = null) : EnroException(message, cause) {
        companion object {
            fun logForStrictMode(navigationController: NavigationController, args: ExecutorArgs<*,*,*>) {
                val message = "Attempted to Push to ${args.key::class.java.simpleName}, but could not find a valid container."
                if(navigationController.isStrictMode) {
                    throw MissingContainerForPushInstruction(message)
                }
                else {
                    Log.w("Enro", "$message Enro opened this NavigationKey as Present, but would have thrown in strict mode.")
                }
            }
        }
    }

    class UnreachableState : EnroException("This state is expected to be unreachable. If you are seeing this exception, please report an issue (with the stacktrace included) at https://github.com/isaac-udy/Enro/issues")

    class ComposePreviewException(message: String) : EnroException(message)

}
