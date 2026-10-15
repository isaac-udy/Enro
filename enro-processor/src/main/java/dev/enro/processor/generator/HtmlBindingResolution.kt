package dev.enro.processor.generator

import dev.enro.processor.domain.GeneratedBindingReference

/**
 * Selects the bindings a Compose HTML navigation component registers: the platform override for
 * each overridden key, so the overridden Compose UI binding is unreferenced and dead-code
 * elimination removes it.
 */
class HtmlBindingResolution(allBindings: List<GeneratedBindingReference>) {

    private val overriddenKeys = allBindings
        .filter { it.isPlatformOverride }
        .map { it.navigationKey }
        .toSet()

    val bindings: List<GeneratedBindingReference> = allBindings
        .filter { it.isPlatformOverride || it.navigationKey !in overriddenKeys }

    // Only a platform override is taken to be a Compose HTML destination. A property destination's
    // builder isn't visible to KSP, so it can't be classified either way.
    val composeUiBindings: List<GeneratedBindingReference> = bindings
        .filter { !it.isPlatformOverride && it.isComposableFunction }

    val unverifiedBindings: List<GeneratedBindingReference> = bindings
        .filter { !it.isPlatformOverride && !it.isComposableFunction }

    fun composeUiError(componentName: String): String? {
        if (composeUiBindings.isEmpty()) return null
        return "$componentName renders with Compose HTML, but these destinations are Compose UI functions:\n" +
            composeUiBindings.describe() +
            "\nAdd a @NavigationDestination.PlatformOverride Compose HTML destination for each key."
    }

    fun unverifiedWarning(componentName: String): String? {
        if (unverifiedBindings.isEmpty()) return null
        return "$componentName renders with Compose HTML. Enro can't check whether these destinations use Compose UI:\n" +
            unverifiedBindings.describe() +
            "\nAdd a @NavigationDestination.PlatformOverride Compose HTML destination for each one that does."
    }

    private fun List<GeneratedBindingReference>.describe(): String {
        return sortedBy { it.navigationKey }
            .joinToString(separator = "\n") { "  - ${it.navigationKey}: ${it.destination}" }
    }
}
