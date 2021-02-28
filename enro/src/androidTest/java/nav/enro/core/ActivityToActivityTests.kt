package nav.enro.core

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import nav.enro.*
import nav.enro.DefaultActivity
import org.junit.Test
import java.util.*

class ActivityToActivityTests {

    @Test
    fun givenDefaultActivityOpenedWithoutNavigationKeySet_thenDefaultKeyIsUsed() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()
        assertEquals(DefaultActivity.defaultKey, handle.key)
    }

    @Test
    fun givenDefaultActivityRecreated_thenNavigationHandleIdIsStable() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val id = scenario.getNavigationHandle<DefaultActivityKey>().id
        scenario.recreate()

        val recreatedId = expectActivity<DefaultActivity>().getNavigationHandle().id
        assertEquals(id, recreatedId)
    }

    @Test
    fun givenDefaultActivity_whenCloseInstructionIsExecuted_thenNoActivitiesAreOpen() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()
        handle.close()
        expectNoActivity()
    }

    @Test
    fun givenDefaultActivity_whenNavigationInstructionIsExecuted_thenCorrectActivityIsOpened() {
        val id = UUID.randomUUID().toString()

        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()
        handle.forward(GenericActivityKey(id))

        val next = expectActivity<GenericActivity>()
        val nextHandle = next.getNavigationHandle().asTyped<GenericActivityKey>()

        assertEquals(id, nextHandle.key.id)
    }

    @Test
    fun givenActivityOpenedWithChildren_thenFinalOpenedActivityIsLastChild() {
        val id = UUID.randomUUID().toString()

        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()
        handle.executeInstruction(
            NavigationInstruction.Forward(
                GenericActivityKey(UUID.randomUUID().toString()),
                listOf(
                    GenericActivityKey(UUID.randomUUID().toString()),
                    GenericActivityKey(UUID.randomUUID().toString()),
                    GenericActivityKey(UUID.randomUUID().toString()),
                    GenericActivityKey(id)
                )
            )
        )

        expectActivity<GenericActivity> {
            it.getNavigationHandle().asTyped<GenericActivityKey>().key.id == id
        }
    }

    @Test
    fun givenDefaultActivity_whenSpecificActivityIsOpened_andThenSpecificActivityIsClosed_thenDefaultActivityIsOpen() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()
        handle.forward(GenericActivityKey("close"))

        val next = expectActivity<GenericActivity>()
        val nextHandle = next.getNavigationHandle()
        nextHandle.close()

        val activeActivity = expectActivity<DefaultActivity>()
        val activeHandle = activeActivity.getNavigationHandle().asTyped<DefaultActivityKey>()
        assertEquals(DefaultActivity.defaultKey, activeHandle.key)
    }

    @Test(expected = IllegalStateException::class)
    fun givenActivityDoesNotHaveDefaultKey_whenActivityOpenedWithoutNavigationKeySet_thenNavigationHandleCannotRetrieveKey() {
        val scenario = ActivityScenario.launch(GenericActivity::class.java)
        val handle = scenario.getNavigationHandle<GenericActivityKey>()
        assertNotNull(handle.key)
    }

    @Test
    fun whenSpecificActivityOpenedWithNavigationKeySet_thenNavigationKeyIsAvailable() {
        val id = UUID.randomUUID().toString()
        val intent =
            Intent(
                InstrumentationRegistry.getInstrumentation().context,
                GenericActivity::class.java
            )
                .addOpenInstruction(
                    NavigationInstruction.Replace(
                        navigationKey = GenericActivityKey(id)
                    )
                )

        val scenario = ActivityScenario.launch<GenericActivity>(intent)
        val handle = scenario.getNavigationHandle<GenericActivityKey>()

        assertEquals(id, handle.key.id)
    }

    @Test
    fun whenActivityIsReplaced_andReplacementIsClosed_thenNoActivitiesAreOpen() {
        val id = UUID.randomUUID().toString()

        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()
        handle.replace(GenericActivityKey(id))

        val next = expectActivity<GenericActivity>()
        val nextHandle = next.getNavigationHandle()

        nextHandle.close()
        expectNoActivity()
    }

    @Test
    fun whenActivityIsReplaced_andActivityHasParent_andReplacementIsClosed_thenParentIsOpen() {
        val first = UUID.randomUUID().toString()
        val second = UUID.randomUUID().toString()

        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()
        handle.forward(GenericActivityKey(first))

        val firstActivity = expectActivity<GenericActivity> { it.getNavigationHandle().asTyped<GenericActivityKey>().key.id == first }
        firstActivity.getNavigationHandle().replace(GenericActivityKey(second))

        val secondActivity = expectActivity<GenericActivity> { it.getNavigationHandle().asTyped<GenericActivityKey>().key.id == second }
        secondActivity.getNavigationHandle().close()

        expectActivity<DefaultActivity>()
    }
}