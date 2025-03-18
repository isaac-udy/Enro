@file:Suppress("PackageDirectoryMismatch")

package dev.enro.core


@Deprecated("You should use push or present")
public fun NavigationHandle.forward(key: NavigationKey) {
    executeInstruction(NavigationInstruction.Forward(key))
}

@Deprecated("You should use a push or present followed by a close instruction")
public fun NavigationHandle.replace(key: NavigationKey) {
    executeInstruction(NavigationInstruction.Replace(key))
}

@Deprecated("You should only use replaceRoot with a NavigationKey.SupportsPresent")
public fun NavigationHandle.replaceRoot(key: NavigationKey, vararg childKeys: NavigationKey) {
    executeInstruction(NavigationInstruction.ReplaceRoot(key))
}