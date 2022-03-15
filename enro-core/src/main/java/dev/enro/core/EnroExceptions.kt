package dev.enro.core

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

    class UnreachableState : EnroException("This state is expected to be unreachable. If you are seeing this exception, please report an issue (with the stacktrace included) at https://github.com/isaac-udy/Enro/issues")
}
