package dev.enro.core

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

public abstract class NavigationBinding<KeyType : NavigationKey, ContextType : Any> {
    public abstract val keyType: KClass<KeyType>
    public abstract val keySerializer: KSerializer<KeyType>
    public abstract val destinationType: KClass<ContextType>
    public abstract val baseType: KClass<in ContextType>

    internal var isPlatformOverride: Boolean = false

    public companion object {
        private const val USE_ORIGINAL_NAVIGATION_BINDING = "dev.enro.core.NavigationBinding.USE_ORIGINAL_NAVIGATION_BINDING"

        /**
         * Sets the [USE_ORIGINAL_NAVIGATION_BINDING] extra to true in the provided [extras].
         *
         * This is used to indicate that the original navigation binding should be used, in the
         * case where the navigation binding is overridden by a platform-specific implementation.
         *
         * This allows a platform-specific implementation to internally use the original navigation
         * binding for certain operations, while still allowing the platform-specific implementation
         * to be navigated to by default.
         *
         * For example, a platform override for the desktop may want to create a new window for a
         * specific navigation destination, and put the original navigation binding in a container
         * within that window.
         */
        internal fun setExtrasToUseOriginalBinding(
            extras: NavigationInstructionExtras,
        ): NavigationInstructionExtras {
            return NavigationInstructionExtras().apply {
                putAll(extras)
                put(USE_ORIGINAL_NAVIGATION_BINDING, true)
            }
        }

        /**
         * Returns true if the [extras] contains the [USE_ORIGINAL_NAVIGATION_BINDING] extra,
         * and it is set to true, which indicates that if a platform override exists for
         * the navigation binding, the original navigation binding should be used instead.
         */
        internal fun shouldUseOriginalBinding(
            extras: NavigationInstructionExtras,
        ): Boolean {
            return extras.get<Boolean>(USE_ORIGINAL_NAVIGATION_BINDING) == true
        }
    }
}
