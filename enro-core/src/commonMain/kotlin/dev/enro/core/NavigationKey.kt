package dev.enro.core

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

public interface NavigationKey {
    public interface WithResult<T: Any> : NavigationKey

    public interface SupportsPush : NavigationKey {
        public interface WithResult<T: Any> : SupportsPush, NavigationKey.WithResult<T>
    }

    public interface SupportsPresent : NavigationKey {
        public interface WithResult<T: Any> : SupportsPresent, NavigationKey.WithResult<T>
    }

    public data class WithExtras<T: NavigationKey> internal constructor(
        val navigationKey: T,
        val extras: NavigationInstructionExtras,
    )

    public companion object
}

@OptIn(InternalSerializationApi::class)
public fun <T: NavigationKey> T.withExtra(
    key: String,
    value: Any,
): NavigationKey.WithExtras<T> {
    return NavigationKey.WithExtras(
        navigationKey = this,
        extras = NavigationInstructionExtras().apply {
            put(key, value::class.serializer() as KSerializer<Any>, value)
        }
    )
}

@OptIn(InternalSerializationApi::class)
public fun <T: NavigationKey> NavigationKey.WithExtras<T>.withExtra(
    key: String,
    value: Any,
): NavigationKey.WithExtras<T> {
    return NavigationKey.WithExtras(
        navigationKey = navigationKey,
        extras = NavigationInstructionExtras().apply {
            putAll(extras)
            put(key, value::class.serializer() as KSerializer<Any>, value)
        }
    )
}

/**
 * The EnroInternalNavigationKey interface is a marker interface that is present on all NavigationKeys
 * that are defined within the Enro library.
 *
 * There are several NavigationKey types which are used internally by Enro.
 * Often, these NavigationKeys are used to wrap NavigationInstructions/Keys to display them
 * in a different context.
 *
 * This is useful when you are generically inspecting a NavigationKey and would like to know whether
 * or not this is a NavigationKey unique to your codebase. For example, you may want to add an
 * EnroPlugin that logs "screen viewed" analytics for your screens when a NavigationHandle becomes active.
 * In these cases, you likely want to ignore NavigationHandles that have a NavigationKey that implements
 * InternalEnroNavigationKey.
 */
public interface EnroInternalNavigationKey