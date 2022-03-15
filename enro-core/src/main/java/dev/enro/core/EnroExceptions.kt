package dev.enro.core

abstract class EnroException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    class InvalidLifecycleState(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class NoAttachedNavigationHandle(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class CouldNotCreateEnroViewModel(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class MissingNavigator(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class IncorrectlyTypedNavigationHandle(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class InvalidViewForNavigationHandle(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class DestinationIsNotDialogDestination(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class EnroResultIsNotInstalled(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class ReceivedIncorrectlyTypedResult(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class NavigationControllerIsNotAttached(message: String, cause: Throwable? = null) : EnroException(message, cause)

    class UnreachableState : EnroException("This state is expected to be unreachable. If you are seeing this exception, please report an issue (with the stacktrace included) at https://github.com/isaac-udy/Enro/issues")
}
