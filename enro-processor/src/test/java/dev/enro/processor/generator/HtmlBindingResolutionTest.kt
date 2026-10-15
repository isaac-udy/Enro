package dev.enro.processor.generator

import dev.enro.processor.domain.GeneratedBindingReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HtmlBindingResolutionTest {

    @Test
    fun overrideReplacesTheBindingForItsKey() {
        val common = binding("app.Home", "app.ui.HomeScreen", isComposableFunction = true)
        val override = binding("app.Home", "app.html.HomeScreen", isPlatformOverride = true, isComposableFunction = true)
        val other = binding("app.Settings", "app.html.SettingsScreen", isPlatformOverride = true)

        val resolution = HtmlBindingResolution(listOf(common, override, other))

        assertEquals(listOf(override, other), resolution.bindings)
    }

    @Test
    fun overrideWithoutACommonBindingIsRegistered() {
        val override = binding("app.HtmlOnly", "app.html.HtmlOnlyScreen", isPlatformOverride = true, isComposableFunction = true)

        val resolution = HtmlBindingResolution(listOf(override))

        assertEquals(listOf(override), resolution.bindings)
        assertNull(resolution.composeUiError("AppComponent"))
        assertNull(resolution.unverifiedWarning("AppComponent"))
    }

    @Test
    fun composableFunctionsWithoutAnOverrideAreAnError() {
        val resolution = HtmlBindingResolution(
            listOf(
                binding("app.Settings", "app.ui.SettingsScreen", isComposableFunction = true),
                binding("app.Home", "app.ui.HomeScreen", isComposableFunction = true),
                binding("app.Profile", "app.ui.ProfileScreen", isComposableFunction = true),
                binding("app.Profile", "app.html.ProfileScreen", isPlatformOverride = true, isComposableFunction = true),
            )
        )

        assertEquals(
            """
                AppComponent renders with Compose HTML, but these destinations are Compose UI functions:
                  - app.Home: app.ui.HomeScreen
                  - app.Settings: app.ui.SettingsScreen
                Add a @NavigationDestination.PlatformOverride Compose HTML destination for each key.
            """.trimIndent(),
            resolution.composeUiError("AppComponent"),
        )
    }

    @Test
    fun propertyDestinationsWithoutAnOverrideAreAWarning() {
        val resolution = HtmlBindingResolution(
            listOf(
                binding("app.Confirm", "app.ui.confirmDestination"),
                binding("app.Flow", "app.ui.flowDestination"),
                binding("app.Flow", "app.html.flowDestination", isPlatformOverride = true),
            )
        )

        assertNull(resolution.composeUiError("AppComponent"))
        assertEquals(
            """
                AppComponent renders with Compose HTML. Enro can't check whether these destinations use Compose UI:
                  - app.Confirm: app.ui.confirmDestination
                Add a @NavigationDestination.PlatformOverride Compose HTML destination for each one that does.
            """.trimIndent(),
            resolution.unverifiedWarning("AppComponent"),
        )
    }

    private fun binding(
        navigationKey: String,
        destination: String,
        isPlatformOverride: Boolean = false,
        isComposableFunction: Boolean = false,
    ) = GeneratedBindingReference(
        qualifiedName = "_${destination.replace(".", "_")}_GeneratedNavigationBinding",
        destination = destination,
        navigationKey = navigationKey,
        isPlatformOverride = isPlatformOverride,
        isComposableFunction = isComposableFunction,
        containingFile = null,
    )
}
