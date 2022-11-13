package dev.enro.core.controller.usecase

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.*
import dev.enro.core.NavigationHandleConfigurationProperties
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.handle.NavigationHandleViewModel
import dev.enro.core.usecase.ConfigureNavigationHandle
import dev.enro.fragment.container.navigationContainer

internal class ConfigureNavigationHandleImpl : ConfigureNavigationHandle {
    override operator fun <T: NavigationKey> invoke(
        configuration: NavigationHandleConfigurationProperties<T>,
        navigationHandle: NavigationHandle
    ) {
        configureContainers(configuration, navigationHandle)
        configureCloseRequested(configuration, navigationHandle)
    }

    private fun <T: NavigationKey> configureCloseRequested(
        configuration: NavigationHandleConfigurationProperties<T>,
        initialNavigationHandle: NavigationHandle
    ) {
        val navigationHandle = if (initialNavigationHandle is TypedNavigationHandleImpl<*>) {
            initialNavigationHandle.navigationHandle
        } else initialNavigationHandle

        val onCloseRequested = configuration.onCloseRequested ?: return

        if (navigationHandle is NavigationHandleViewModel) {
            navigationHandle.internalOnCloseRequested =
                { onCloseRequested(navigationHandle.asTyped(configuration.keyType)) }
        } else if (navigationHandle.dependencyScope.get<NavigationController>().isInTest) {
            val field = navigationHandle::class.java.declaredFields
                .firstOrNull { it.name.startsWith("internalOnCloseRequested") }
                ?: return
            field.isAccessible = true
            field.set(navigationHandle, { onCloseRequested(navigationHandle.asTyped(configuration.keyType)) })
        }
    }

    private fun configureContainers(
        configuration: NavigationHandleConfigurationProperties<*>,
        initialNavigationHandle: NavigationHandle
    ) {
        val navigationHandle = when(initialNavigationHandle) {
            is TypedNavigationHandleImpl<*> -> initialNavigationHandle.navigationHandle
            else -> null
        }

        val context = when(navigationHandle) {
            is NavigationHandleViewModel -> navigationHandle.navigationContext
            else -> return
        }
        requireNotNull(context)

        configuration.childContainers.forEach {
            val container = when(context.contextReference) {
                is FragmentActivity -> {
                    context.contextReference.navigationContainer(
                        containerId = it.containerId,
                        accept = it::accept
                    )
                }
                is Fragment -> {
                    context.contextReference.navigationContainer(
                        containerId = it.containerId,
                        accept = it::accept
                    )
                }
                else -> return@forEach
            }
            // trigger container creation
            container.navigationContainer.hashCode()
        }
    }
}