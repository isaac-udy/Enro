package dev.enro.core.controller

import dev.enro.android.NavigationApplication


@Suppress("UNCHECKED_CAST")
internal fun NavigationApplication.loadGeneratedNavigationModule(): NavigationModule  {
    val moduleScopeAction = runCatching {
        Class.forName(this::class.java.name + "Navigation")
            .newInstance() as NavigationModuleScope.() -> Unit
    }.getOrDefault(defaultValue = {})

    return createNavigationModule(moduleScopeAction)
}