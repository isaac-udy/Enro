@file:Suppress("TYPEALIAS_EXPANSION_DEPRECATION", "DEPRECATION")
package dev.enro.core

import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState

public typealias AnyOpenInstruction = NavigationInstructionOpen
public typealias OpenPushInstruction = NavigationInstructionOpen
public typealias OpenPresentInstruction = NavigationInstructionOpen

public typealias NavigationInstructionOpen = dev.enro.NavigationKey.Instance<dev.enro.NavigationKey>

@Deprecated("Use dev.enro.NavigationKey.Instance instead")
public object NavigationInstruction {
    public fun Push(navigationKey: NavigationKey.SupportsPush): NavigationInstructionOpen {
        return navigationKey.asPush()
    }

    public fun Present(navigationKey: NavigationKey.SupportsPresent): NavigationInstructionOpen {
        return navigationKey.asPresent()
    }

    public fun Push(navigationKey: dev.enro.NavigationKey.WithMetadata<out NavigationKey.SupportsPush>): NavigationInstructionOpen {
        return navigationKey.key.asPush().apply {
            metadata.setFrom(navigationKey.metadata)
        }
    }

    public fun Present(navigationKey: dev.enro.NavigationKey.WithMetadata<out NavigationKey.SupportsPresent>): NavigationInstructionOpen {
        return navigationKey.key.asPresent().apply {
            metadata.setFrom(navigationKey.metadata)
        }
    }

    public object Parceler : kotlinx.parcelize.Parceler<NavigationInstructionOpen> {
        override fun create(parcel: android.os.Parcel): NavigationInstructionOpen {
            val savedState = parcel.readBundle(this::class.java.classLoader) ?: throw IllegalArgumentException("Saved state bundle is null")
            return decodeFromSavedState<NavigationInstructionOpen>(savedState, dev.enro.EnroController.savedStateConfiguration)
        }

        override fun NavigationInstructionOpen.write(parcel: android.os.Parcel, flags: Int) {
            val savedState = encodeToSavedState(this@write, dev.enro.EnroController.savedStateConfiguration)
            parcel.writeBundle(savedState)
        }
    }
}

public val AnyOpenInstruction.navigationDirection: NavigationDirection
    get() {
        return metadata.get(NavigationDirection.MetadataKey) ?: NavigationDirection.Push
    }

internal fun AnyOpenInstruction.setNavigationDirection(navigationDirection: NavigationDirection) {
    metadata.set(NavigationDirection.MetadataKey, navigationDirection)
}
