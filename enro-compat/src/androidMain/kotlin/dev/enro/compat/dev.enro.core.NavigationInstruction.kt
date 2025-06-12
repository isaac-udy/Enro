@file:Suppress("TYPEALIAS_EXPANSION_DEPRECATION", "DEPRECATION")
package dev.enro.core

public typealias AnyOpenInstruction = NavigationInstructionOpen
public typealias OpenPushInstruction = NavigationInstructionOpen
public typealias OpenPresentInstruction = NavigationInstructionOpen

public typealias NavigationInstructionOpen = dev.enro.NavigationKey.Instance<NavigationKey>

@Deprecated("Use dev.enro.NavigationKey.Instance instead")
public object NavigationInstruction {
    public fun Push(key: NavigationKey.SupportsPush): NavigationInstructionOpen {
        return key.asPush()
    }
    public fun Present(key: NavigationKey.SupportsPresent): NavigationInstructionOpen {
        return key.asPresent()
    }
}