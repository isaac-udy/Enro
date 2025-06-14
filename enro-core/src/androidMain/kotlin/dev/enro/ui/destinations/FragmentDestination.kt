package dev.enro.ui.destinations

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.rememberFragmentState
import dev.enro.NavigationKey
import dev.enro.platform.putNavigationKeyInstance
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDestinationScope
import dev.enro.ui.destinations.fragment.AndroidDialogFragment
import dev.enro.ui.destinations.fragment.FragmentNavigationHandle
import dev.enro.ui.destinations.fragment.fragmentContextHolder
import dev.enro.ui.navigationDestination
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

public inline fun <reified T : NavigationKey, reified F : Fragment> fragmentDestination(
    noinline metadata: NavigationDestination.MetadataBuilder<T>.() -> Unit = {},
    noinline arguments: NavigationDestinationScope<T>.() -> Bundle = { Bundle() },
): NavigationDestinationProvider<T> {
    return fragmentDestination(
        keyType = T::class,
        fragmentType = F::class,
        metadata = metadata,
        arguments = arguments,
    )
}

public fun <T : NavigationKey, F : Fragment> fragmentDestination(
    keyType: KClass<T>,
    fragmentType: KClass<F>,
    metadata: NavigationDestination.MetadataBuilder<T>.() -> Unit = {},
    arguments: NavigationDestinationScope<T>.() -> Bundle = { Bundle() },
): NavigationDestinationProvider<T> {
    return navigationDestination(metadata) {
        key(navigation.instance.id) {
            var fragment: F? by remember {
                mutableStateOf(null)
            }
            val fragmentState = rememberFragmentState()
            if (fragmentType.isSubclassOf(DialogFragment::class)) {
                AndroidDialogFragment(
                    clazz = fragmentType.java as Class<DialogFragment>,
                    tag = navigation.instance.id,
                    fragmentState = fragmentState,
                    arguments = arguments().apply {
                        putNavigationKeyInstance(navigation.instance)
                    },
                ) { f ->
                    fragment = f as F
                }
            } else {
                AndroidFragment(
                    clazz = fragmentType.java,
                    modifier = Modifier.fillMaxSize(),
                    fragmentState = fragmentState,
                    arguments = arguments().apply {
                        putNavigationKeyInstance(navigation.instance)
                    },
                ) { f ->
                    fragment = f
                }
            }
            DisposableEffect(fragment) {
                val fragment = fragment
                if (fragment == null) return@DisposableEffect onDispose { }
                val navigation = fragment.fragmentContextHolder.navigationHandle
                @Suppress("UNCHECKED_CAST")
                navigation as FragmentNavigationHandle<T>
                navigation.bind(this@navigationDestination)
                onDispose {
                    navigation.unbind()
                }
            }
        }
    }
}
