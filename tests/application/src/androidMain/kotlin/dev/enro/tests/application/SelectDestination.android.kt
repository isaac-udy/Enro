@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.tests.application

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.repository.NavigationBindingRepository
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

actual fun loadNavigationDestinations(controller: NavigationController): List<ReflectedDestination> {
    val bindingRepository = controller.dependencyScope.get<NavigationBindingRepository>()

    @Suppress("UNCHECKED_CAST")
    val bindingsByKeyType = NavigationBindingRepository::class.declaredMemberProperties
        .first { it.name == "bindingsByKeyType" }
        .apply { isAccessible = true }
        .call(bindingRepository) as Map<KClass<*>, NavigationBinding<*, *>>

    return bindingsByKeyType.keys
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
                        if (index > 0 && c.isUpperCase()) {
                            return@mapIndexed " $c"
                        }
                        return@mapIndexed c.toString()
                    }
                    .joinToString(separator = "")
            )
        }
        .sortedBy { it.title }

}