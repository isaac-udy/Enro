package dev.enro

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import dev.enro.context.RootContext
import dev.enro.controller.createNavigationModule
import dev.enro.handle.DestinationNavigationHandle
import dev.enro.test.EnroTest
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.fixtures.TestLifecycleAndViewModelStoreOwner
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.navigationDestination
import dev.enro.ui.navigationDestinationContent
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DeferredNavigationOperationTests {

    @Test
    fun `an open issued during a destination's first composition reaches the container inside it`() = runEnroComposeTest {
        var innerContainer: NavigationContainerState? = null
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<DeferredParentKey>(
                    navigationDestination<DeferredParentKey> {
                        val inner = rememberNavigationContainer(
                            backstack = emptyBackstack(),
                            emptyBehavior = EmptyBehavior.allowEmpty(),
                        )
                        innerContainer = inner
                        remember { navigation.open(DeferredChildKey) }
                        NavigationDisplay(inner)
                    }
                )
                destination<DeferredChildKey>(
                    navigationDestination<DeferredChildKey> { Text("child") }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()
        var outerContainer: NavigationContainerState? = null

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val outer = rememberNavigationContainer(
                    backstack = backstackOf(DeferredParentKey.asInstance()),
                )
                outerContainer = outer
                NavigationDisplay(outer)
            }
        }
        waitForIdle()

        assertEquals(listOf<NavigationKey>(DeferredParentKey), outerContainer!!.backstack.map { it.key })
        assertEquals(listOf<NavigationKey>(DeferredChildKey), innerContainer!!.backstack.map { it.key })
    }

    @Test
    fun `an operation from a handle whose host was destroyed runs once a new context attaches`() = runEnroComposeTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<NavigationKeyFixtures.SimpleKey>(navigationDestinationContent { })
            }
        )
        runOnUiThread {
            val destination = NavigationDestinationFixtures.create<NavigationKey>(NavigationKeyFixtures.SimpleKey())
            val handle = DestinationNavigationHandle(destination.instance, SavedStateHandle())

            val firstHost = TestLifecycleAndViewModelStoreOwner()
            val firstRoot = createRootContext(firstHost)
            val firstContainer = NavigationContextFixtures.createContainerContext(firstRoot)
            firstContainer.container.setFilter(acceptAll())
            firstContainer.container.setBackstackDirect(backstackOf(destination.instance))
            val firstContext = NavigationContextFixtures.createDestinationContext(firstContainer, destination)
            firstRoot.registerChild(firstContainer)
            firstContainer.registerChild(firstContext)
            handle.bindContext(firstContext)
            handle.attachContext(firstContext)

            firstContainer.unregisterChild(firstContext)
            firstRoot.unregisterChild(firstContainer)
            firstHost.setLifecycleState(Lifecycle.State.CREATED)
            firstHost.setLifecycleState(Lifecycle.State.DESTROYED)

            val opened = NavigationKeyFixtures.SimpleKey()
            handle.execute(NavigationOperation.Open(opened.asInstance()))
            assertEquals(listOf(destination.instance.id), firstContainer.container.backstack.map { it.id })

            val secondRoot = createRootContext(TestLifecycleAndViewModelStoreOwner())
            val secondContainer = NavigationContextFixtures.createContainerContext(secondRoot)
            secondContainer.container.setFilter(acceptAll())
            secondContainer.container.setBackstackDirect(backstackOf(destination.instance))
            val secondContext = NavigationContextFixtures.createDestinationContext(secondContainer, destination)
            secondRoot.registerChild(secondContainer)
            secondContainer.registerChild(secondContext)
            handle.bindContext(secondContext)
            assertEquals(1, secondContainer.container.backstack.size)

            handle.attachContext(secondContext)
            assertEquals(listOf<NavigationKey>(destination.key, opened), secondContainer.container.backstack.map { it.key })
        }
    }

    private fun createRootContext(owner: TestLifecycleAndViewModelStoreOwner): RootContext {
        return RootContext(
            id = "DeferredNavigationOperationTests@${owner.hashCode()}",
            parent = Unit,
            controller = EnroTest.getCurrentNavigationController(),
            lifecycleOwner = owner,
            viewModelStoreOwner = owner,
            defaultViewModelProviderFactory = owner,
            activeChildId = mutableStateOf(null),
        )
    }
}

@Serializable
data object DeferredParentKey : NavigationKey

@Serializable
data object DeferredChildKey : NavigationKey
