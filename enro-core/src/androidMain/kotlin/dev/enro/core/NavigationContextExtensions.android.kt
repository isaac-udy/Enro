package dev.enro.core

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dagger.hilt.internal.GeneratedComponentManager
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.destination.compose.ComposableDestination

public val NavigationContext<out Fragment>.fragment: Fragment get() = contextReference

public fun NavigationContext<*>.activeChildContext(): NavigationContext<*>? {
    val fragmentManager = when (contextReference) {
        is FragmentActivity -> contextReference.supportFragmentManager
        is Fragment -> contextReference.childFragmentManager
        else -> null
    }
    return containerManager.activeContainer?.childContext
        ?: fragmentManager?.primaryNavigationFragment?.navigationContext
}

public val ComponentActivity.containerManager: NavigationContainerManager get() = navigationContext.containerManager
public val Fragment.containerManager: NavigationContainerManager get() = navigationContext.containerManager
public val ComposableDestinationReference.containerManager: NavigationContainerManager get() = navigationContext.containerManager

public val Fragment.parentContainer: NavigationContainer? get() = navigationContext.parentContainer()

public val NavigationContext<*>.activity: ComponentActivity
    get() {
        return when (val ref = contextReference) {
            is ComponentActivity -> ref
            is Fragment -> ref.requireActivity()
            is ComposableDestination -> {
                // Get the activity from the owner
                val activity = getActivityForComposable(ref)
                activity
            }
            else -> throw EnroException.UnreachableState()
        }
    }

// Helper function to get activity from ComposableDestination
private fun getActivityForComposable(composable: ComposableDestination): ComponentActivity {
    // Access activity through the destination's context using a different path
    // to avoid direct reference to ComposableDestinationOwner
    return composable.navigationContext.findParentActivity()
}

// Find parent activity through navigation context hierarchy
private fun NavigationContext<*>.findParentActivity(): ComponentActivity {
    val ref = contextReference
    return when {
        ref is ComponentActivity -> ref
        ref is Fragment -> ref.requireActivity()
        parentContext != null -> parentContext!!.findParentActivity()
        else -> throw EnroException.UnreachableState() // Use the no-arg constructor
    }
}

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
public val <T : ComponentActivity> T.navigationContext: NavigationContext<T>
    get() = getNavigationHandleViewModel().navigationContext as NavigationContext<T>

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
public val <T : Fragment> T.navigationContext: NavigationContext<T>
    get() = getNavigationHandleViewModel().navigationContext as NavigationContext<T>

private val generatedComponentManagerHolderClass by lazy {
    runCatching {
        GeneratedComponentManagerHolder::class.java
    }.getOrNull()
}

internal val NavigationContext<*>.isHiltContext
    get() = if (generatedComponentManagerHolderClass != null) {
        activity is GeneratedComponentManagerHolder
    } else false

private val generatedComponentManagerClass by lazy {
    runCatching {
        GeneratedComponentManager::class.java
    }.getOrNull()
}

internal val NavigationContext<*>.isHiltApplication
    get() = if (generatedComponentManagerClass != null) {
        activity.application is GeneratedComponentManager<*>
    } else false