package dev.enro

import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.controller.createNavigationModule
import dev.enro.path.NavigationPathBinding
import dev.enro.path.PathData
import dev.enro.path.createPathBinding
import dev.enro.path.fromBinding
import dev.enro.path.getNavigationKeyFromPath
import dev.enro.path.getPathFromNavigationKey
import dev.enro.test.EnroTest
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.runEnroTest
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
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

    @Test
    fun `getPathFromNavigationKey serializes a key back to its registered path`() = runEnroTest {
        val binding = NavigationPathBinding.createPathBinding<String, ProductDetailPathKey>(
            pattern = "products/{productId}",
            propertyOne = ProductDetailPathKey::productId,
            constructor = { id -> ProductDetailPathKey(id) },
        )
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule { path(binding) }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        val path = rootContext.getPathFromNavigationKey(ProductDetailPathKey("p-42"))

        assertEquals(
            expected = "/products/p-42",
            actual = path,
            message = "Key should serialize through the registered binding into its URL form",
        )
    }

    @Test
    fun `getPathFromNavigationKey returns null when no binding is registered for the key`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(createNavigationModule { })
        val rootContext = NavigationContextFixtures.createRootContext()

        val path = rootContext.getPathFromNavigationKey(ProductDetailPathKey("any-id"))

        assertNull(
            actual = path,
            message = "Without a registered binding, key->path should return null rather than throw",
        )
    }

    @OptIn(ExperimentalEnroApi::class)
    @Test
    fun `NavigationKey PathBinding round-trips a key through fromBinding helper`() = runEnroTest {
        val binding = NavigationPathBinding.fromBinding(
            keyType = ProductDetailPathKey::class,
            binding = ProductDetailPathKeyBinding,
        )
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule { path(binding) }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        val resolved = rootContext.getNavigationKeyFromPath("/binding/products?id=p-7")
        val serialized = rootContext.getPathFromNavigationKey(ProductDetailPathKey("p-7"))

        assertEquals(
            expected = ProductDetailPathKey("p-7"),
            actual = resolved,
            message = "fromBinding should drive the deserialize side via the user's PathBinding",
        )
        assertEquals(
            expected = "/binding/products?id=p-7",
            actual = serialized,
            message = "fromBinding should drive the serialize side via the user's PathBinding",
        )
    }

    @Test
    fun `Value class path parameters round-trip through an explicit NavigationPathBinding`() = runEnroTest {
        val binding = NavigationPathBinding(
            keyType = ValueClassPathKey::class,
            pattern = "/customers/{id}?count={count?}",
            deserialize = {
                ValueClassPathKey(
                    id = CustomerIdValue(require("id")),
                    count = optional("count")?.toInt()?.let { CountValue(it) },
                )
            },
            serialize = { key ->
                set("id", key.id.value)
                key.count?.let { v -> set("count", v.raw.toString()) }
            },
        )
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule { path(binding) }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        val resolved = rootContext.getNavigationKeyFromPath("/customers/cust-1?count=3")
        val serialized = rootContext.getPathFromNavigationKey(
            ValueClassPathKey(CustomerIdValue("cust-1"), CountValue(3)),
        )

        assertEquals(
            expected = ValueClassPathKey(CustomerIdValue("cust-1"), CountValue(3)),
            actual = resolved,
        )
        assertEquals(
            expected = "/customers/cust-1?count=3",
            actual = serialized,
        )
    }
}

@Serializable
data class ProductDetailPathKey(val productId: String) : NavigationKey

@Serializable
data object SettingsPathKey : NavigationKey

@Serializable
data object AnotherPathKey : NavigationKey

@OptIn(ExperimentalEnroApi::class)
private object ProductDetailPathKeyBinding : NavigationKey.PathBinding<ProductDetailPathKey> {
    override val pattern: String = "/binding/products?id={id?}"
    override fun deserialize(data: PathData): ProductDetailPathKey {
        return ProductDetailPathKey(productId = data.optional("id") ?: "missing")
    }
    override fun serialize(builder: PathData.Builder, key: ProductDetailPathKey) {
        builder.set("id", key.productId)
    }
}

@JvmInline
@Serializable
value class CustomerIdValue(val value: String)

@JvmInline
@Serializable
value class CountValue(val raw: Int)

@Serializable
data class ValueClassPathKey(
    val id: CustomerIdValue,
    val count: CountValue? = null,
) : NavigationKey
