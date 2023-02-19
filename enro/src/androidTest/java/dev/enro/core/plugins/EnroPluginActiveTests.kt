package dev.enro.core.plugins

import androidx.test.core.app.ActivityScenario
import dev.enro.GenericFragmentKey
import dev.enro.TestActivity
import dev.enro.TestPlugin
import dev.enro.core.container.setActive
import dev.enro.core.containerManager
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.getNavigationHandle
import dev.enro.core.push
import dev.enro.expectActivity
import junit.framework.TestCase.assertEquals
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test

class EnroPluginActiveTests {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenMultipleFragmentContainers_whenAFragmentContainerIsMadeActive_thenTheActiveNavigationHandleInThatContainerIsMarkedActive() {
        val scenario = ActivityScenario.launch(MultipleFragmentContainerActivity::class.java)
        val activity = expectActivity<MultipleFragmentContainerActivity>()

        val primaryKey = GenericFragmentKey("primary")
        val secondaryKey = GenericFragmentKey("secondary")

        scenario.onActivity {
            assertEquals(activity.primaryContainer, activity.containerManager.activeContainer)

            activity.getNavigationHandle().push(primaryKey)
            activity.getNavigationHandle().push(secondaryKey)
            assertEquals(activity.secondaryContainer, activity.containerManager.activeContainer)

            activity.primaryContainer.setActive()
            assertEquals(activity.primaryContainer, activity.containerManager.activeContainer)
            assertEquals(primaryKey, TestPlugin.activeKey)

            activity.secondaryContainer.setActive()
            assertEquals(activity.secondaryContainer, activity.containerManager.activeContainer)
            assertEquals(secondaryKey, TestPlugin.activeKey)

            activity.primaryContainer.setActive()
            assertEquals(activity.primaryContainer, activity.containerManager.activeContainer)
            assertEquals(primaryKey, TestPlugin.activeKey)
        }
    }
}

class MultipleFragmentContainerActivity() : TestActivity() {
    val primaryContainer by navigationContainer(
        containerId = primaryFragmentContainer,
        accept = { it is GenericFragmentKey && it.id.startsWith("primary") }
    )
    val secondaryContainer by navigationContainer(
        containerId = secondaryFragmentContainer,
        accept = { it is GenericFragmentKey && it.id.startsWith("secondary") }
    )
}