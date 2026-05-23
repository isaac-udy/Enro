package dev.enro

import dev.enro.controller.createNavigationModule
import dev.enro.path.NavigationPathBinding
import dev.enro.path.createPathBinding
import dev.enro.path.getNavigationKeyFromPath
import dev.enro.test.EnroTest
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.runEnroTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for the path-binding lookup that happens through the
 * controller's [PathRepository]. The existing [path.NavigationPathBindingTests]
 * covers path-pattern matching and key↔path conversion in isolation; these
 * tests assert that bindings registered via [createNavigationModule] are
 * routable via [getNavigationKeyFromPath] from any navigation context.
 */
class PathBindingIntegrationTests {

    @Test
    fun `getNavigationKeyFromPath resolves a registered binding into the right key`() = runEnroTest {
        val binding = NavigationPathBinding.createPathBinding<String, ProductDetailPathKey>(
            pattern = "products/{productId}",
            propertyOne = ProductDetailPathKey::productId,
            constructor = { id -> ProductDetailPathKey(id) },
        )
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule { path(binding) }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        val resolved = rootContext.getNavigationKeyFromPath("products/abc-123")

        assertEquals(
            expected = ProductDetailPathKey("abc-123"),
            actual = resolved,
            message = "Path binding should deserialize the {productId} segment into the right key",
        )
    }

    @Test
    fun `getNavigationKeyFromPath returns null when no binding matches the path`() = runEnroTest {
        val binding = NavigationPathBinding.createPathBinding<SettingsPathKey>(
            pattern = "settings",
            constructor = { SettingsPathKey },
        )
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule { path(binding) }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        val resolved = rootContext.getNavigationKeyFromPath("totally/unknown/path")

        assertNull(
            actual = resolved,
            message = "An unmatched path should resolve to null rather than throw",
        )
    }

    @Test
    fun `getNavigationKeyFromPath disambiguates between multiple bindings by pattern`() = runEnroTest {
        val productBinding = NavigationPathBinding.createPathBinding<String, ProductDetailPathKey>(
            pattern = "products/{productId}",
            propertyOne = ProductDetailPathKey::productId,
            constructor = { id -> ProductDetailPathKey(id) },
        )
        val settingsBinding = NavigationPathBinding.createPathBinding<SettingsPathKey>(
            pattern = "settings",
            constructor = { SettingsPathKey },
        )

        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                path(productBinding)
                path(settingsBinding)
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        assertEquals(
            expected = ProductDetailPathKey("p-7"),
            actual = rootContext.getNavigationKeyFromPath("products/p-7"),
            message = "Product path should resolve to the product key",
        )
        assertEquals(
            expected = SettingsPathKey,
            actual = rootContext.getNavigationKeyFromPath("settings"),
            message = "Settings path should resolve to the settings key",
        )
    }

    @Test
    fun `Two bindings that match the same path throw IllegalArgumentException when resolving`() = runEnroTest {
        val first = NavigationPathBinding.createPathBinding<SettingsPathKey>(
            pattern = "ambiguous",
            constructor = { SettingsPathKey },
        )
        val second = NavigationPathBinding.createPathBinding<AnotherPathKey>(
            pattern = "ambiguous",
            constructor = { AnotherPathKey },
        )
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                path(first)
                path(second)
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        val error = assertFailsWith<IllegalArgumentException> {
            rootContext.getNavigationKeyFromPath("ambiguous")
        }
        assertTrue(
            actual = error.message?.contains("Multiple path bindings") == true,
            message = "Error should call out the ambiguity; was: ${error.message}",
        )
    }
}

@Serializable
data class ProductDetailPathKey(val productId: String) : NavigationKey

@Serializable
data object SettingsPathKey : NavigationKey

@Serializable
data object AnotherPathKey : NavigationKey
