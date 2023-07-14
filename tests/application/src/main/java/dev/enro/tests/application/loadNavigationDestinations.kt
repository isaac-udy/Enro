package dev.enro.tests.application

import android.app.Application
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.navigationController
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility

data class ReflectedDestination(
    val pushInstance: NavigationKey.SupportsPush?,
    val presentInstance: NavigationKey.SupportsPresent?,
    val title: String,
)

// This is a hacky method of loading all NavigationKeys available,
// so that we can render a destination picker easily in the test application
// this uses slow reflection to work, and should not be used in a production application
fun loadNavigationDestinations(
    application: Application
): List<ReflectedDestination> {
    val controller = application.navigationController
    val bindingRepository = NavigationController::class.java
        .declaredFields
        .first {
            it.name.startsWith("navigationBindingRepository")
        }
        .apply {
            isAccessible = true
        }
        .get(controller)

    val keysMap = bindingRepository::class.java
        .declaredFields
        .first {
            it.name.startsWith("bindingsByKeyType")
        }
        .apply {
            isAccessible = true
        }
        .get(bindingRepository) as Map<KClass<out NavigationKey>, *>

    return keysMap.keys
        .filter {
            it.visibility == KVisibility.PUBLIC
        }
        .filter {
            it.objectInstance != null ||
                    it.constructors.any { it.parameters.all { it.isOptional } }
        }
        .map {
            it.objectInstance
                ?: it.constructors.first { it.parameters.all { it.isOptional } }.call()
        }
        .filter { it is NavigationKey.SupportsPresent || it is NavigationKey.SupportsPush }
        .map {
            ReflectedDestination(
                pushInstance = it as? NavigationKey.SupportsPush,
                presentInstance = it as? NavigationKey.SupportsPresent,
                title = it::class.simpleName!!.toCharArray()
                    .mapIndexed { index, c ->
                        if(index > 0 && c.isUpperCase()) {
                            return@mapIndexed " $c"
                        }
                        return@mapIndexed c.toString()
                    }
                    .joinToString(separator = "")
            )
        }
}