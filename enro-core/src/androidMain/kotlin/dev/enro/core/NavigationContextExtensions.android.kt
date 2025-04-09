package dev.enro.core

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dagger.hilt.internal.GeneratedComponentManager
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.destination.compose.ComposableDestination

public val NavigationContext<out Fragment>.fragment: Fragment get() = contextReference

@AdvancedEnroApi
public fun NavigationContext<*>.directParentContainer(): NavigationContainer? {
    val parentContext = parentContext ?: return null
    val instructionId = getNavigationHandle().id
    return parentContext.containerManager.containers.firstOrNull { container ->
        container.backstack.any { it.instructionId == instructionId }
    }
}

public fun NavigationContext<*>.activeChildContext(): NavigationContext<*>? {
    val fragmentManager = when (contextReference) {
        is FragmentActivity -> contextReference.supportFragmentManager
        is Fragment -> contextReference.childFragmentManager
        else -> null
    }
    return containerManager.activeContainer?.childContext
        ?: fragmentManager?.primaryNavigationFragment?.navigationContext
}

public actual fun NavigationContext<*>.leafContext(): NavigationContext<*> {
    // TODO This currently includes inactive contexts, should it only check for actual active contexts?
    val fragmentManager = when (contextReference) {
        is FragmentActivity -> contextReference.supportFragmentManager
        is Fragment -> contextReference.childFragmentManager
        else -> null
    }
    return containerManager.activeContainer?.childContext?.leafContext()
        ?: runCatching { fragmentManager?.primaryNavigationFragment?.navigationContext }.getOrNull()?.leafContext()
        ?: this
}

public val ComponentActivity.containerManager: NavigationContainerManager get() = navigationContext.containerManager
public val Fragment.containerManager: NavigationContainerManager get() = navigationContext.containerManager
public val ComposableDestinationReference.containerManager: NavigationContainerManager get() = navigationContext.containerManager

public actual val containerManager: NavigationContainerManager
    @Composable
    get() {
        val viewModelStoreOwner = LocalViewModelStoreOwner.current!!

        val context = LocalContext.current
        val view = LocalView.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // The navigation context attached to a NavigationHandle may change when the Context, View,
        // or LifecycleOwner changes, so we're going to re-query the navigation context whenever
        // any of these change, to ensure the container always has an up-to-date NavigationContext
        return remember(context, view, lifecycleOwner) {
            viewModelStoreOwner
                .navigationContext!!
                .containerManager
        }
    }

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

internal actual val ViewModelStoreOwner.navigationContext: NavigationContext<*>?
    get() = getNavigationHandleViewModel().navigationContext

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