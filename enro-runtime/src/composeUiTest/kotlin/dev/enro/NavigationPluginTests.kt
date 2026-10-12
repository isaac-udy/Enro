package dev.enro

import androidx.compose.material3.Text
import dev.enro.controller.createNavigationModule
import dev.enro.plugin.NavigationPlugin
import dev.enro.test.EnroTest
import dev.enro.test.runEnroTest
import dev.enro.ui.NavigationDestination
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Tests for the [NavigationPlugin] SPI and its [PluginRepository] wiring.
 * Plugins are the framework's extension point for cross-cutting hooks
 * like analytics, lifecycle stamping, or destination-metadata rewriting
 * — none of which had unit coverage before.
 */
class NavigationPluginTests {

    @Test
    fun `Plugin added via addModule receives onAttached when controller is already installed`() = runEnroTest {
        val attachedControllers = mutableListOf<EnroController>()
        val plugin = object : NavigationPlugin() {
            override fun onAttached(controller: EnroController) {
                attachedControllers += controller
            }
        }

        val controller = EnroTest.getCurrentNavigationController()
        controller.addModule(createNavigationModule { plugin(plugin) })

        assertEquals(
            expected = 1,
            actual = attachedControllers.size,
            message = "Plugin onAttached should fire once when the plugin is added to an already-installed controller",
        )
        assertSame(controller, attachedControllers.single())
    }

    @Test
    fun `Plugin onDetached fires when controller is uninstalled`() = runEnroTest {
        var detachedCount = 0
        val plugin = object : NavigationPlugin() {
            override fun onDetached(controller: EnroController) {
                detachedCount++
            }
        }

        EnroTest.getCurrentNavigationController()
            .addModule(createNavigationModule { plugin(plugin) })

        // Uninstall manually inside the test so we can observe onDetached
        // running while runEnroTest's finally still does the no-op cleanup.
        EnroTest.uninstallNavigationController()

        assertEquals(
            expected = 1,
            actual = detachedCount,
            message = "Plugin onDetached should fire exactly once when the controller is uninstalled",
        )
    }

    @Test
    fun `Multiple plugins receive onAttached in registration order`() = runEnroTest {
        val events = mutableListOf<String>()
        val pluginA = object : NavigationPlugin() {
            override fun onAttached(controller: EnroController) {
                events += "A"
            }
        }
        val pluginB = object : NavigationPlugin() {
            override fun onAttached(controller: EnroController) {
                events += "B"
            }
        }
        val pluginC = object : NavigationPlugin() {
            override fun onAttached(controller: EnroController) {
                events += "C"
            }
        }

        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                plugin(pluginA)
                plugin(pluginB)
                plugin(pluginC)
            }
        )

        assertEquals(
            expected = listOf("A", "B", "C"),
            actual = events,
            message = "Plugins should be notified in the order they were registered with the module",
        )
    }

    @Test
    fun `onDestinationCreated additionalMetadata is applied to the destination`() = runEnroTest {
        val plugin = object : NavigationPlugin() {
            override fun onDestinationCreated(
                destination: NavigationDestination<*>,
                additionalMetadata: MutableMap<String, Any?>,
            ) {
                additionalMetadata["plugin-added-key"] = "plugin-added-value"
            }
        }

        val controller = EnroTest.getCurrentNavigationController()
        controller.addModule(
            createNavigationModule {
                plugin(plugin)
                destination<TestPluginKey>(
                    navigationDestination<TestPluginKey> { Text("plugin test") }
                )
            }
        )

        val instance = TestPluginKey.asInstance()
        val destination = controller.bindings.destinationFor(instance)

        assertEquals(
            expected = "plugin-added-value",
            actual = destination.metadata["plugin-added-key"],
            message = "Metadata added by the plugin's onDestinationCreated should be present on the resolved destination",
        )
    }
}

@Serializable
data object TestPluginKey : NavigationKey
