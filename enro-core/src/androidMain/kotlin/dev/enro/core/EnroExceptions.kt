package dev.enro.core

import android.util.Log
import dev.enro.core.container.ExecutorArgs
import dev.enro.core.controller.NavigationController

public abstract class EnroException(
    private val inputMessage: String, cause: Throwable? = null
) : RuntimeException(cause) {
    override val message: String?
        get() = "${
            inputMessage.trim().removeSuffix(".")
        }. See https://github.com/isaac-udy/Enro/blob/main/docs/troubleshooting.md#${this::class.simpleName} for troubleshooting help"

    public class NoAttachedNavigationHandle(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class CouldNotCreateEnroViewModel(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class ViewModelCouldNotGetNavigationHandle(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class MissingNavigationBinding(navigationKey: NavigationKey) :
        EnroException("Could not find a valid navigation binding for ${navigationKey::class.simpleName}")

    public class IncorrectlyTypedNavigationHandle(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class InvalidViewForNavigationHandle(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class DestinationIsNotDialogDestination(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class EnroResultIsNotInstalled(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class ResultChannelIsNotInitialised(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class ReceivedIncorrectlyTypedResult(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class NavigationControllerIsNotAttached(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class NavigationContainerWrongThread(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class LegacyNavigationDirectionUsedInStrictMode(
        message: String,
        cause: Throwable? = null
    ) : EnroException(message, cause) {
        internal companion object {
            fun logForStrictMode(
                navigationController: NavigationController,
                args: ExecutorArgs<*, *, *>
            ) {
                when (args.instruction.navigationDirection) {
                    NavigationDirection.Present,
                    NavigationDirection.Push,
                    NavigationDirection.ReplaceRoot -> return
                    else -> { /* continue */
                    }
                }

                val message =
                    "Opened ${args.key::class.simpleName} as a ${args.instruction.navigationDirection::class.simpleName} instruction. Forward and Replace type instructions are deprecated, please replace these with Push and Present instructions."
                if (navigationController.config.isStrictMode) {
                    throw LegacyNavigationDirectionUsedInStrictMode(message)
                } else {
                    Log.w("Enro", "$message Enro would have thrown in strict mode.")
                }
            }
        }
    }

    public class MissingContainerForPushInstruction(message: String, cause: Throwable? = null) :
        EnroException(message, cause) {
        internal companion object {
            fun logForStrictMode(
                navigationController: NavigationController,
                navigationKey: NavigationKey
            ) {
                val message =
                    "Attempted to Push to ${navigationKey::class.simpleName}, but could not find a valid container."
                if (navigationController.config.isStrictMode) {
                    throw MissingContainerForPushInstruction(message)
                } else {
                    Log.w(
                        "Enro",
                        "$message Enro opened this NavigationKey as Present, but would have thrown in strict mode."
                    )
                }
            }
        }
    }

    public class UnreachableState :
        EnroException("This state is expected to be unreachable. If you are seeing this exception, please report an issue (with the stacktrace included) at https://github.com/isaac-udy/Enro/issues")

    public class ComposePreviewException(message: String) : EnroException(message)

    public class DuplicateFragmentNavigationContainer(message: String, cause: Throwable? = null) :
        EnroException(message, cause)

    public class CannotCreateHostForType(targetContextType: Class<*>, originalContextType: Class<*>) : EnroException("Could not find a host that would host a ${originalContextType.simpleName} in a ${targetContextType.simpleName}. If you are seeing this exception and are using Composable, Activity or Fragment navigation, something has gone seriously wrong, and you should report an issue at https://github.com/isaac-udy/Enro/issues. If you are attempting to use custom navigation context types, this may be an issue with your implementation.")

}
