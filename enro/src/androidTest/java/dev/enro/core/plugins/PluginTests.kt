package dev.enro.core.plugins

import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.forward
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import junit.framework.TestCase.assertEquals
import kotlinx.parcelize.Parcelize
import org.junit.Test
import java.util.*

class PluginTests {

    @Test
    fun whenActivityIsStarted_thenActivityIsActive() {
        ActivityScenario.launch(PluginTestActivity::class.java)

        assertEquals(
            expectContext<PluginTestActivity, PluginTestActivityKey>()
                .navigation
                .key,
            TestPlugin.activeKey
        )
    }

    @Test
    fun whenFragmentIsStarted_thenFragmentIsActive() {
        ActivityScenario.launch(PluginTestActivity::class.java)

        expectContext<PluginTestActivity, PluginTestActivityKey>()
            .navigation
            .forward(PluginPrimaryTestFragmentKey())

        val context = expectContext<PluginPrimaryTestFragment, PluginPrimaryTestFragmentKey>()
        waitFor { context.navigation.key == TestPlugin.activeKey }
    }

    @Test
    fun whenFragmentIsStartedAndClosed_thenActivityIsActive() {
        ActivityScenario.launch(PluginTestActivity::class.java)

        expectContext<PluginTestActivity, PluginTestActivityKey>()
            .navigation
            .forward(PluginPrimaryTestFragmentKey())

        expectContext<PluginPrimaryTestFragment, PluginPrimaryTestFragmentKey>()
            .navigation
            .close()

        val context = expectContext<PluginTestActivity, PluginTestActivityKey>()
        waitFor { context.navigation.key == TestPlugin.activeKey }

    }

    @Test
    fun whenFragmentIsStarted_thenSecondaryFragmentIsStarted_thenSecondaryFragmentIsActive() {
        ActivityScenario.launch(PluginTestActivity::class.java)

        val activityNavigation = expectContext<PluginTestActivity, PluginTestActivityKey>()
            .navigation

        activityNavigation.forward(PluginPrimaryTestFragmentKey())
        expectContext<PluginPrimaryTestFragment, PluginPrimaryTestFragmentKey>()

        activityNavigation.forward(PluginSecondaryTestFragmentKey())

        val context = expectContext<PluginSecondaryTestFragment, PluginSecondaryTestFragmentKey>()
        waitFor {
            context.navigation.key == TestPlugin.activeKey
        }
    }

    @Test
    fun whenFragmentIsStarted_thenSecondaryFragmentIsStartedAndClosed_thenPrimaryFragmentIsActive() {
        ActivityScenario.launch(PluginTestActivity::class.java)

        val activityNavigation = expectContext<PluginTestActivity, PluginTestActivityKey>()
            .navigation

        activityNavigation.forward(PluginPrimaryTestFragmentKey())
        expectContext<PluginPrimaryTestFragment, PluginPrimaryTestFragmentKey>()

        activityNavigation.forward(PluginSecondaryTestFragmentKey())
        expectContext<PluginSecondaryTestFragment, PluginSecondaryTestFragmentKey>()
            .navigation
            .close()

        val context = expectContext<PluginPrimaryTestFragment, PluginPrimaryTestFragmentKey>()
        waitFor {
            context.navigation.key == TestPlugin.activeKey
        }
    }

    @Test
    fun whenFragmentIsStartedWithNestedChild_thenSecondaryFragmentIsStartedAndClosed_thenPrimaryFragmentIsActive() {
        ActivityScenario.launch(PluginTestActivity::class.java)

        val activityNavigation = expectContext<PluginTestActivity, PluginTestActivityKey>()
            .navigation

        activityNavigation.forward(PluginPrimaryTestFragmentKey())
        expectContext<PluginPrimaryTestFragment, PluginPrimaryTestFragmentKey>()
            .navigation
            .forward(PluginPrimaryTestFragmentKey("nested"))

        activityNavigation.forward(PluginSecondaryTestFragmentKey())
        expectContext<PluginSecondaryTestFragment, PluginSecondaryTestFragmentKey>()
            .navigation
            .close()

        val context = expectContext<PluginPrimaryTestFragment, PluginPrimaryTestFragmentKey> {
            it.navigation.key.keyId == "nested"
        }
        waitFor {
            context.navigation.key == TestPlugin.activeKey
        }
    }
}

@Parcelize
data class PluginTestActivityKey(val keyId: String = UUID.randomUUID().toString()) : NavigationKey

@NavigationDestination(PluginTestActivityKey::class)
class PluginTestActivity : TestActivity() {
    private val navigation by navigationHandle<PluginTestActivityKey> {
        defaultKey(PluginTestActivityKey())
    }
    private val primaryContainer by navigationContainer(primaryFragmentContainer) {
        it is PluginPrimaryTestFragmentKey
    }

    private val secondaryContainer by navigationContainer(secondaryFragmentContainer) {
        it is PluginSecondaryTestFragmentKey
    }
}

@Parcelize
data class PluginPrimaryTestFragmentKey(val keyId: String = UUID.randomUUID().toString()) :
    NavigationKey

@NavigationDestination(PluginPrimaryTestFragmentKey::class)
class PluginPrimaryTestFragment : TestFragment() {
    private val navigation by navigationHandle<PluginTestActivityKey> ()
    private val primaryContainer by navigationContainer(primaryFragmentContainer) {
        it is PluginPrimaryTestFragmentKey
    }

    private val secondaryContainer by navigationContainer(secondaryFragmentContainer) {
        it is PluginSecondaryTestFragmentKey
    }
}

@Parcelize
data class PluginSecondaryTestFragmentKey(val keyId: String = UUID.randomUUID().toString()) :
    NavigationKey

@NavigationDestination(PluginSecondaryTestFragmentKey::class)
class PluginSecondaryTestFragment : TestFragment() {
    private val navigation by navigationHandle<PluginTestActivityKey>()

    private val primaryContainer by navigationContainer(primaryFragmentContainer) {
        it is PluginPrimaryTestFragmentKey
    }

    private val secondaryContainer by navigationContainer(secondaryFragmentContainer) {
        it is PluginSecondaryTestFragmentKey
    }
}