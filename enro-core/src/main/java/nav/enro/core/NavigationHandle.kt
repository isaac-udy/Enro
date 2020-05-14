package nav.enro.core

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import nav.enro.core.controller.NavigationController
import nav.enro.core.internal.handle.NavigationHandleViewModel

interface NavigationHandle<T : NavigationKey> : LifecycleOwner {
    val id: String
    val key: T
    val controller: NavigationController
    val additionalData: Bundle
    fun executeInstruction(navigationInstruction: NavigationInstruction)
    fun onCloseRequested(onCloseRequested: () -> Unit)
}

fun NavigationHandle<*>.forward(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Open(NavigationDirection.FORWARD, key, childKeys.toList()))

fun NavigationHandle<*>.replace(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Open(NavigationDirection.REPLACE, key, childKeys.toList()))

fun NavigationHandle<*>.replaceRoot(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Open(NavigationDirection.REPLACE_ROOT, key, childKeys.toList()))

fun NavigationHandle<*>.close() =
    executeInstruction(NavigationInstruction.Close)

